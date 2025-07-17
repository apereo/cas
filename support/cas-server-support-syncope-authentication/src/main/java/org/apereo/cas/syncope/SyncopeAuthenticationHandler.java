package org.apereo.cas.syncope;

import org.apache.hc.core5.http.ProtocolException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
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
            Principal principal = principalFactory.createPrincipal(user.get("username").asText(),
                    SyncopeUtils.convertFromUserEntity(user, properties.getAttributeMappings()));
            return createHandlerResult(credential, principal, new ArrayList<>());
        }
        throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
    }

    protected Optional<JsonNode> authenticateSyncopeUser(final UsernamePasswordCredential credential)
            throws AccountPasswordMustChangeException, AccountDisabledException {
        HttpResponse response = null;
        try {
            String syncopeRestUrl = StringUtils.appendIfMissing(
                    SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()),
                    "/rest/users/self");
            HttpExecutionRequest exec = HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(syncopeRestUrl)
                    .basicAuthUsername(credential.getUsername())
                    .basicAuthPassword(credential.toPassword())
                    .headers(CollectionUtils.wrap("X-Syncope-Domain", syncopeDomain))
                    .maximumRetryAttempts(properties.getMaxRetryAttempts())
                    .build();
            response = Objects.requireNonNull(SyncopeUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getReasonPhrase());
            if (response.getCode() == HttpStatus.SC_OK) {
                return parseResponseResults((HttpEntityContainer) response);
            }
            val header = response.getHeader("x-application-error-info");
            if (header.getValue().contains("change your password")) {
                throw new AccountPasswordMustChangeException(
                        "Account password must change for " + credential.getUsername());
            } else if (header.getValue().contains("suspended")) {
                throw new AccountDisabledException(
                        "Could not authenticate forbidden account for " + credential.getUsername());
            }
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
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
