package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
public class DuoSecurityAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final DuoSecurityMultifactorAuthenticationProvider provider;

    public DuoSecurityAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory,
                                            final DuoSecurityMultifactorAuthenticationProvider provider,
                                            final Integer order) {
        super(name, servicesManager, principalFactory, order);
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
        if (credential instanceof DuoSecurityDirectCredential) {
            LOGGER.debug("Attempting to directly authenticate credential against Duo");
            return authenticateDuoApiCredential(credential);
        }
        return authenticateDuoCredential(credential);
    }

    private AuthenticationHandlerExecutionResult authenticateDuoApiCredential(final Credential credential) throws FailedLoginException {
        try {
            val duoAuthenticationService = provider.getDuoAuthenticationService();
            val creds = DuoSecurityDirectCredential.class.cast(credential);
            if (duoAuthenticationService.authenticate(creds).getKey()) {
                val principal = creds.getAuthentication().getPrincipal();
                LOGGER.debug("Duo has successfully authenticated [{}]", principal.getId());
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        throw new FailedLoginException("Duo authentication has failed");
    }

    private AuthenticationHandlerExecutionResult authenticateDuoCredential(final Credential credential) throws FailedLoginException {
        try {
            val duoCredential = (DuoSecurityCredential) credential;
            if (!duoCredential.isValid()) {
                throw new GeneralSecurityException("Duo credential validation failed. Ensure a username "
                    + " and the signed Duo response is configured and passed. Credential received: " + duoCredential);
            }

            val duoAuthenticationService = provider.getDuoAuthenticationService();
            val duoVerifyResponse = duoAuthenticationService.authenticate(duoCredential).getValue();
            LOGGER.debug("Response from Duo verify: [{}]", duoVerifyResponse);
            val primaryCredentialsUsername = duoCredential.getUsername();

            val isGoodAuthentication = duoVerifyResponse.equals(primaryCredentialsUsername);

            if (isGoodAuthentication) {
                LOGGER.info("Successful Duo authentication for [{}]", primaryCredentialsUsername);

                val principal = this.principalFactory.createPrincipal(duoVerifyResponse);
                return createHandlerResult(credential, principal, new ArrayList<>(0));
            }
            throw new FailedLoginException("Duo authentication username "
                + primaryCredentialsUsername + " does not match Duo response: " + duoVerifyResponse);

        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw new FailedLoginException(e.getMessage());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        if (credential instanceof MultifactorAuthenticationCredential) {
            val id = ((MultifactorAuthenticationCredential) credential).getProviderId();
            return provider.validateId(id);
        }
        return false;
    }
}
