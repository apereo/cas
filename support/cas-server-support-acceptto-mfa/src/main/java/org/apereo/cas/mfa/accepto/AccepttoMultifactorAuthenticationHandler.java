package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.mfa.AccepttoMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AccepttoMultifactorAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AccepttoMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final AccepttoMultifactorAuthenticationProperties accepttoProperties;

    public AccepttoMultifactorAuthenticationHandler(final ServicesManager servicesManager,
                                                    final PrincipalFactory principalFactory,
                                                    final AccepttoMultifactorAuthenticationProperties accepttoProperties) {
        super(accepttoProperties.getName(),
            servicesManager,
            principalFactory,
            accepttoProperties.getOrder());
        this.accepttoProperties = accepttoProperties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential)
        throws GeneralSecurityException {
        try {
            val url = StringUtils.appendIfMissing(accepttoProperties.getApiUrl(), "/") + "check";

            val tokenCredential = (AccepttoMultifactorTokenCredential) credential;
            LOGGER.debug("Received token [{}]", tokenCredential.getId());

            val authentication = WebUtils.getInProgressAuthentication();
            val attributes = authentication.getPrincipal().getAttributes();
            val email = CollectionUtils.firstElement(attributes.get(accepttoProperties.getEmailAttribute()))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Unable to determine email address"));

            LOGGER.debug("Email determined from attribute [{}] is [{}]", accepttoProperties.getEmailAttribute(), email);
            val parameters = CollectionUtils.<String, Object>wrap(
                "uid", accepttoProperties.getApplicationId(),
                "secret", accepttoProperties.getSecret(),
                "email", email,
                "channel", tokenCredential.getId());

            HttpResponse response = null;
            try {
                val exec = HttpUtils.HttpExecutionRequest.builder()
                    .method(HttpMethod.POST)
                    .url(url)
                    .parameters(parameters)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null) {
                    val status = response.getStatusLine().getStatusCode();
                    if (status == HttpStatus.SC_OK) {
                        val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                        val results = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                        LOGGER.debug("Received results as [{}]", results);

                        val channelStatus = results.get("status").toString();

                        if ("expired".equalsIgnoreCase(channelStatus)) {
                            throw new AccountExpiredException("Authentication request has expired");
                        }
                        if ("declined".equalsIgnoreCase(channelStatus)) {
                            throw new FailedLoginException("Acceptto authentication has been declined");
                        }

                        if ("approved".equalsIgnoreCase(channelStatus)) {
                            val deviceId = results.get("device_id").toString();
                            val attr = CollectionUtils.<String, List<Object>>wrap(
                                "accepttoChannel", CollectionUtils.wrapList(tokenCredential.getId()),
                                "accepttoDeviceId", CollectionUtils.wrapList(deviceId),
                                "accepttoStatus", CollectionUtils.wrapList(channelStatus));
                            val principal = this.principalFactory.createPrincipal(email, attr);
                            return createHandlerResult(tokenCredential, principal);
                        }
                    }
                    if (status == HttpStatus.SC_FORBIDDEN) {
                        throw new AccountNotFoundException("Invalid uid and secret combination; application not found");
                    }
                    if (status == HttpStatus.SC_UNAUTHORIZED) {
                        throw new AccountLockedException("Email address provided is not a valid registered account");
                    }
                }
            } finally {
                HttpUtils.close(response);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Acceptto authentication has failed");
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return AccepttoMultifactorTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return AccepttoMultifactorTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
