package org.jasig.cas.adaptors.duo;

import org.jasig.cas.authentication.MessageDescriptor;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Authenticate CAS credentials against Duo Security.
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Component("duoAuthenticationHandler")
public final class DuoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final DuoAuthenticationService duoAuthenticationService;

    /**
     * Creates the duo authentication handler.
     * @param duoAuthenticationService the duo authentication service
     */
    @Autowired
    public DuoAuthenticationHandler(@Qualifier("duoAuthenticationService")
                                        final DuoAuthenticationService duoAuthenticationService) {
        this.duoAuthenticationService = duoAuthenticationService;
    }


    /**
     * Do an out of band request using the DuoWeb api (encapsulated in DuoAuthenticationService)
     * to the hosted duo service. If it is successful
     * it will return a String containing the username of the successfully authenticated user, but if not - will
     * return a blank String or null.
     * @param credential Credential to authenticate.
     *
     * @throws GeneralSecurityException general security exception for errors
     * @throws PreventedException authentication failed exception
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
                return createHandlerResult(credential, principal, new ArrayList<MessageDescriptor>());
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
}
