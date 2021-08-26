package org.apereo.cas.gauth;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * An authentication handler that uses the token provided
 * to authenticator against google authN for MFA.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class GoogleAuthenticatorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    public GoogleAuthenticatorAuthenticationHandler(final String name,
                                                    final ServicesManager servicesManager,
                                                    final PrincipalFactory principalFactory,
                                                    final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator,
                                                    final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.validator = validator;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return GoogleAuthenticatorTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return GoogleAuthenticatorTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential)
        throws GeneralSecurityException, PreventedException {

        val tokenCredential = (GoogleAuthenticatorTokenCredential) credential;
        val authentication = WebUtils.getInProgressAuthentication();

        val validatedToken = validator.validate(authentication, tokenCredential);

        if (validatedToken != null) {
            val principal = authentication.getPrincipal().getId();
            LOGGER.debug("Validated OTP token [{}] successfully for [{}]", validatedToken, principal);
            validator.store(validatedToken);
            LOGGER.debug("Creating authentication result and building principal for [{}]", principal);
            return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(principal));
        }
        LOGGER.warn("Authorization of OTP token [{}] has failed", credential);
        throw new FailedLoginException("Failed to authenticate code " + credential);
    }
}
