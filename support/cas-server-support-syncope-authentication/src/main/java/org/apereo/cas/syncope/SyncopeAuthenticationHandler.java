package org.apereo.cas.syncope;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;

import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SyncopeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SyncopeAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    private static final String MUST_CHANGE_PASSWORD_RESPONSE = "Please change your password first";
    
    private static final String SUSPENDED_RESPONSE = "is suspended";

    private final SyncopeAuthenticationProperties properties;

    private final String syncopeDomain;

    public SyncopeAuthenticationHandler(final SyncopeAuthenticationProperties properties,
                                        final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory,
                                        final String syncopeDomain) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.properties = properties;
        this.syncopeDomain = syncopeDomain;
    }

    @Override
    @SneakyThrows
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) {
        val result = authenticateSyncopeUser(credential);
        if (result.isPresent()) {
            val user = result.get();
            LOGGER.debug("Received user object as [{}]", user);
            if (user.has("suspended") && user.get("suspended").asBoolean()) {
                throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
            }
            if (user.has("mustChangePassword") && user.get("mustChangePassword").asBoolean()) {
                throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
            }
            val principal = this.principalFactory.createPrincipal(user.get("username").asText(),
                SyncopeUserTOConverterUtils.convert(user, properties.getAttributeMappings()));
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
    }

    protected Optional<JsonNode> authenticateSyncopeUser(final UsernamePasswordCredential credential) throws Exception {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = StringUtils.appendIfMissing(properties.getUrl(), "/rest/users/self");
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(credential.getUsername())
                .basicAuthPassword(credential.toPassword())
                .headers(CollectionUtils.wrap("X-Syncope-Domain", this.syncopeDomain))
                .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK: {
                    val result = EntityUtils.toString(response.getEntity());
                    LOGGER.debug("Received user object as [{}]", result);
                    return Optional.of(MAPPER.readTree(result));
                }
                case HttpStatus.SC_FORBIDDEN: {
                    val header = response.getFirstHeader("x-application-error-info").getValue();
                    LOGGER.debug("Received header error info [{}]", header);
                    ObjectNode result = MAPPER.createObjectNode();
                    if (header.contains(MUST_CHANGE_PASSWORD_RESPONSE)) {
                        LOGGER.debug("User is Must Change Password");
                        result.put("mustChangePassword", 1);
                    }
                    return Optional.of(result);
                }
                case HttpStatus.SC_UNAUTHORIZED: {
                    val header = response.getFirstHeader("x-application-error-info").getValue();
                    LOGGER.debug("Received header error info [{}]", header);
                    ObjectNode result = MAPPER.createObjectNode();
                    if (header.contains(SUSPENDED_RESPONSE)) {
                        LOGGER.debug("User is Suspended");
                        result.put("suspended", 1);
                    }
                    return Optional.of(result);
                }
            }
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }
}
