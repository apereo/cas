package org.apereo.cas.syncope;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationProperties;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.http.HttpMethod;
import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This is {@link SyncopeAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Monitorable
public class SyncopeAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

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
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {
        val result = authenticateSyncopeUser(credential);
        if (result.isPresent()) {
            val user = result.get();
            LOGGER.debug("Received user object as [{}]", user);
            if (user.has("suspended") && user.get("suspended").asBoolean()) {
                throw new AccountDisabledException(
                    "Could not authenticate forbidden account for " + credential.getUsername());
            }
            if (user.has("mustChangePassword") && user.get("mustChangePassword").asBoolean()) {
                throw new AccountPasswordMustChangeException(
                    "Account password must change for " + credential.getUsername());
            }
            val principal = principalFactory.createPrincipal(user.get("username").asText(),
                SyncopeUtils.convertFromUserEntity(user, properties.getAttributeMappings()));
            return createHandlerResult(credential, principal, new ArrayList<>());
        }
        throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
    }

    protected Optional<JsonNode> authenticateSyncopeUser(final UsernamePasswordCredential credential)
        throws Throwable {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = Strings.CI.appendIfMissing(
                SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()),
                "/rest/users/self");
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(credential.getUsername())
                .basicAuthPassword(credential.toPassword())
                .headers(CollectionUtils.wrap(SyncopeUtils.SYNCOPE_HEADER_DOMAIN, syncopeDomain))
                .maximumRetryAttempts(properties.getMaxRetryAttempts())
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                LOGGER.debug("Received http response status as [{}]", response.getCode());
                if (response.getCode() == HttpStatus.SC_FORBIDDEN || response.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                    val appInfoHeader = response.getFirstHeader("X-Application-Error-Info");
                    if (appInfoHeader != null && Strings.CI.equals("Please change your password first", appInfoHeader.getValue())) {
                        val user = MAPPER.createObjectNode();
                        user.put("username", credential.getUsername());
                        user.put("mustChangePassword", true);
                        return Optional.of(user);
                    } else if (appInfoHeader != null
                        && Strings.CI.equals("User" + credential.getUsername() + " is suspended", appInfoHeader.getValue())) {
                        val user = MAPPER.createObjectNode();
                        user.put("username", credential.getUsername());
                        user.put("suspended", true);
                        return Optional.of(user);
                    }
                } else if (response.getCode() == HttpStatus.SC_OK) {
                    return parseResponseResults((HttpEntityContainer) response);
                }
            }
            LOGGER.debug("Received http response with null value");
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }

    private static Optional<JsonNode> parseResponseResults(final HttpEntityContainer response) {
        return FunctionUtils.doUnchecked(() -> {
            val result = EntityUtils.toString(response.getEntity());
            LOGGER.debug("Received user object as [{}]", result);
            return Optional.of(MAPPER.readTree(result));
        });
    }
}
