package org.apereo.cas.authentication;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link PasswordlessTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class PasswordlessTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    public PasswordlessTokenAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                  final PrincipalFactory principalFactory, final Integer order,
                                                  final PasswordlessTokenRepository passwordlessTokenRepository) {
        super(name, servicesManager, principalFactory, order);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val c = (OneTimePasswordCredential) credential;
        val token = passwordlessTokenRepository.findToken(c.getId());

        if (token.isPresent() && token.get().equalsIgnoreCase(c.getPassword())) {
            val principal = principalFactory.createPrincipal(c.getId());
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException("Passwordless authentication has failed");
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return OneTimePasswordCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        if (!(credential instanceof OneTimePasswordCredential)) {
            LOGGER.debug("Credential is not one of one-time password and is not accepted by handler [{}]", getName());
            return false;
        }
        return true;
    }
}
