package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import lombok.val;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * This is {@link WebAuthnAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class WebAuthnAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {
    private final RegistrationStorage webAuthnCredentialRepository;

    private final SessionManager sessionManager;

    public WebAuthnAuthenticationHandler(final String name, final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final RegistrationStorage webAuthnCredentialRepository,
        final SessionManager sessionManager,
        final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.webAuthnCredentialRepository = webAuthnCredentialRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean supports(final Credential credential) {
        return WebAuthnCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return WebAuthnCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val webAuthnCredential = (WebAuthnCredential) credential;
        val authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        val principal = authentication.getPrincipal();
        val uid = principal.getId();
        val credentials = webAuthnCredentialRepository.getCredentialIdsForUsername(principal.getId());
        if (credentials.isEmpty()) {
            throw new AccountNotFoundException("Unable to locate registration record for " + uid);
        }
        val token = WebAuthnCredential.from(webAuthnCredential);
        if (sessionManager.getSession(token).isEmpty()) {
            throw new FailedLoginException("Unable to validate session token " + webAuthnCredential);
        }
        return createHandlerResult(webAuthnCredential, this.principalFactory.createPrincipal(uid));
    }
}
