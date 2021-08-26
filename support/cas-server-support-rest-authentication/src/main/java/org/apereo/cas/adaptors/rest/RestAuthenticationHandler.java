package org.apereo.cas.adaptors.rest;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.configuration.model.support.rest.RestAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link RestAuthenticationHandler} that authenticates uid/password against a remote
 * rest endpoint based on the status code received. Credentials are passed via basic authn.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RestAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /**
     * Header name that explains the password expiration date.
     */
    public static final String HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE = "X-CAS-PasswordExpirationDate";

    /**
     * Header name that explains the warnings.
     */
    public static final String HEADER_NAME_CAS_WARNING = "X-CAS-Warning";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final RestAuthenticationProperties properties;

    public RestAuthenticationHandler(final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory,
                                     final RestAuthenticationProperties properties) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.properties = properties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {

        var response = (HttpResponse) null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthUsername(credential.getUsername())
                .basicAuthPassword(credential.getPassword())
                .method(HttpMethod.POST)
                .url(properties.getUri())
                .build();
            response = HttpUtils.execute(exec);
            val status = HttpStatus.resolve(Objects.requireNonNull(response).getStatusLine().getStatusCode());
            switch (Objects.requireNonNull(status)) {
                case OK:
                    return buildPrincipalFromResponse(credential, response);
                case FORBIDDEN:
                    throw new AccountDisabledException("Could not authenticate forbidden account for " + credential.getUsername());
                case UNAUTHORIZED:
                    throw new FailedLoginException("Could not authenticate account for " + credential.getUsername());
                case NOT_FOUND:
                    throw new AccountNotFoundException("Could not locate account for " + credential.getUsername());
                case LOCKED:
                    throw new AccountLockedException("Could not authenticate locked account for " + credential.getUsername());
                case PRECONDITION_FAILED:
                    throw new AccountExpiredException("Could not authenticate expired account for " + credential.getUsername());
                case PRECONDITION_REQUIRED:
                    throw new AccountPasswordMustChangeException("Account password must change for " + credential.getUsername());
                default:
                    throw new FailedLoginException("Rest endpoint returned an unknown status code " + status + " for " + credential.getUsername());
            }
        } finally {
            HttpUtils.close(response);
        }
    }

    /**
     * Build principal from response.
     *
     * @param credential the credential
     * @param response   the response
     * @return the authentication handler execution result
     * @throws GeneralSecurityException the general security exception
     */
    protected AuthenticationHandlerExecutionResult buildPrincipalFromResponse(final UsernamePasswordCredential credential,
                                                                              final HttpResponse response) throws GeneralSecurityException {
        try {
            val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            LOGGER.debug("REST authentication response received: [{}]", result);
            val principalFromRest = MAPPER.readValue(result, Principal.class);
            val principal = principalFactory.createPrincipal(principalFromRest.getId(), principalFromRest.getAttributes());
            return createHandlerResult(credential, principal, getWarnings(response));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException("Unable to detect the authentication principal for " + credential.getUsername());
        }
    }

    /**
     * Resolve {@link MessageDescriptor warnings} from the response.
     *
     * @param authenticationResponse The response sent by the REST authentication endpoint
     * @return The warnings for the created {@link AuthenticationHandlerExecutionResult}
     */
    protected List<MessageDescriptor> getWarnings(final HttpResponse authenticationResponse) {
        val messageDescriptors = new ArrayList<MessageDescriptor>(2);

        val passwordExpirationDate = authenticationResponse.getFirstHeader(HEADER_NAME_CAS_PASSWORD_EXPIRATION_DATE);
        if (passwordExpirationDate != null) {
            val days = Duration.between(Instant.now(Clock.systemUTC()), DateTimeUtils.convertToZonedDateTime(passwordExpirationDate.getValue())).toDays();
            messageDescriptors.add(new PasswordExpiringWarningMessageDescriptor(null, days));
        }

        val warnings = authenticationResponse.getHeaders(HEADER_NAME_CAS_WARNING);
        if (warnings != null) {
            Arrays.stream(warnings)
                .map(NameValuePair::getValue)
                .map(DefaultMessageDescriptor::new)
                .forEach(messageDescriptors::add);
        }

        return messageDescriptors;
    }
}




