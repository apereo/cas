package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link AccepttoMultifactorAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AccepttoMultifactorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    public AccepttoMultifactorAuthenticationHandler(final String name,
                                                    final ServicesManager servicesManager,
                                                    final PrincipalFactory principalFactory,
                                                    final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential)
        throws GeneralSecurityException {
        try {
            val tokenCredential = (AccepttoMultifactorTokenCredential) credential;
            LOGGER.debug("Received token [{}]", tokenCredential.getId());

            val principal = this.principalFactory.createPrincipal("");
            return createHandlerResult(tokenCredential, principal);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
