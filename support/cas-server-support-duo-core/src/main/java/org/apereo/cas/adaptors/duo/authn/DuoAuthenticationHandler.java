package org.apereo.cas.adaptors.duo.authn;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Authenticate CAS credentials against Duo Security.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Slf4j
public class DuoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final VariegatedMultifactorAuthenticationProvider provider;

    public DuoAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                    final VariegatedMultifactorAuthenticationProvider provider) {
        super(name, servicesManager, principalFactory, null);
        this.provider = provider;
    }

    /**
     * Do an out of band request using the DuoWeb api (encapsulated in DuoSecurityAuthenticationService)
     * to the hosted duo service. If it is successful
     * it will return a String containing the username of the successfully authenticated user, but if not - will
     * return a blank String or null.
     *
     * @param credential Credential to authenticate.
     * @return the result of this handler
     * @throws GeneralSecurityException general security exception for errors
     */
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        if (credential instanceof DuoDirectCredential) {
            LOGGER.debug("Attempting to directly authenticate credential against Duo");
            return authenticateDuoApiCredential(credential);
        }
        return authenticateDuoCredential(credential);
    }

    private AuthenticationHandlerExecutionResult authenticateDuoApiCredential(final Credential credential) throws FailedLoginException {
        try {
            final var duoAuthenticationService = getDuoAuthenticationService();
            final var c = DuoDirectCredential.class.cast(credential);
            if (duoAuthenticationService.authenticate(c).getKey()) {
                final var principal = c.getAuthentication().getPrincipal();
                LOGGER.debug("Duo has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new FailedLoginException("Duo authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoCredential(final Credential credential) throws FailedLoginException {
        try {
            final var duoCredential = (DuoCredential) credential;
            if (!duoCredential.isValid()) {
                throw new GeneralSecurityException("Duo credential validation failed. Ensure a username "
                    + " and the signed Duo response is configured and passed. Credential received: " + duoCredential);
            }

            final var duoAuthenticationService = getDuoAuthenticationService();
            final var duoVerifyResponse = duoAuthenticationService.authenticate(duoCredential).getValue();
            LOGGER.debug("Response from Duo verify: [{}]", duoVerifyResponse);
            final var primaryCredentialsUsername = duoCredential.getUsername();

            final var isGoodAuthentication = duoVerifyResponse.equals(primaryCredentialsUsername);

            if (isGoodAuthentication) {
                LOGGER.info("Successful Duo authentication for [{}]", primaryCredentialsUsername);

                final var principal = this.principalFactory.createPrincipal(duoVerifyResponse);
                return createHandlerResult(credential, principal, new ArrayList<>());
            }
            throw new FailedLoginException("Duo authentication username "
                + primaryCredentialsUsername + " does not match Duo response: " + duoVerifyResponse);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailedLoginException(e.getMessage());
        }
    }

    private DuoSecurityAuthenticationService getDuoAuthenticationService() {
        final var requestContext = RequestContextHolder.getRequestContext();
        if (requestContext == null) {
            throw new IllegalArgumentException("No request context is held to locate the Duo authentication service");
        }
        final var providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        final var providers =
            MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds,
                ApplicationContextProvider.getApplicationContext());

        if (providers.isEmpty()) {
            throw new IllegalArgumentException("No multifactor providers are found in the current request context");
        }
        final var pr = providers.iterator().next();
        return provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class).getDuoAuthenticationService();
    }

    @Override
    public boolean supports(final Credential credential) {
        return DuoCredential.class.isAssignableFrom(credential.getClass())
            || credential instanceof DuoDirectCredential;
    }
}
