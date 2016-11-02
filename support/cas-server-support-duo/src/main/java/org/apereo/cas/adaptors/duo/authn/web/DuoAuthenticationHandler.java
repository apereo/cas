package org.apereo.cas.adaptors.duo.authn.web;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Authenticate CAS credentials against Duo Security.
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public class DuoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private DuoAuthenticationService duoAuthenticationService;

    /**
     * Creates the duo authentication handler.
     */
    public DuoAuthenticationHandler() {}

    /**
     * Do an out of band request using the DuoWeb api (encapsulated in DuoAuthenticationService)
     * to the hosted duo service. If it is successful
     * it will return a String containing the username of the successfully authenticated user, but if not - will
     * return a blank String or null.
     * @param credential Credential to authenticate.
     *
     * @throws GeneralSecurityException general security exception for errors
     * @throws PreventedException authentication failed exception
     * @return the result of this handler
     */
    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        try {
            final DuoCredential duoCredential = (DuoCredential) credential;

            if (!duoCredential.isValid()) {
                throw new GeneralSecurityException("Duo credential validation failed. Ensure a username "
                + " and the signed Duo response is configured and passed. Credential received: " + duoCredential);
            }

            final String duoVerifyResponse = this.duoAuthenticationService.authenticate(duoCredential.getSignedDuoResponse());
            logger.debug("Response from Duo verify: [{}]", duoVerifyResponse);
            final String primaryCredentialsUsername = duoCredential.getUsername();

            final boolean isGoodAuthentication = duoVerifyResponse.equals(primaryCredentialsUsername);

            if (isGoodAuthentication) {
                logger.info("Successful Duo authentication for [{}]", primaryCredentialsUsername);

                final Principal principal = this.principalFactory.createPrincipal(duoVerifyResponse);
                return createHandlerResult(credential, principal, new ArrayList<>());
            }
            throw new FailedLoginException("Duo authentication username "
                    + primaryCredentialsUsername + " does not match Duo response: " + duoVerifyResponse);

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new FailedLoginException(e.getMessage());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return DuoCredential.class.isAssignableFrom(credential.getClass());
    }

    public void setDuoAuthenticationService(final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }
}
