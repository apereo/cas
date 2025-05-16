package org.apereo.cas.gauth;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import javax.security.auth.login.FailedLoginException;
import java.util.Objects;

/**
 * An authentication handler that uses the token provided
 * to authenticator against google authN for MFA.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Monitorable
public class GoogleAuthenticatorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    public GoogleAuthenticatorAuthenticationHandler(
        final String name,
        final ServicesManager servicesManager,
        final PrincipalFactory principalFactory,
        final OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator,
        final Integer order, final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(name, servicesManager, principalFactory, order);
        this.validator = validator;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
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
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val tokenCredential = (GoogleAuthenticatorTokenCredential) credential;
        val authentication = Objects.requireNonNull(WebUtils.getInProgressAuthentication());
        Objects.requireNonNull(authentication, "No authentication is available to determine the principal");
        val validatedToken = validator.validate(authentication, tokenCredential);
        if (validatedToken != null) {
            val principal = authentication.getPrincipal().getId();
            val attributes = authentication.getPrincipal().getAttributes();
            LOGGER.debug("Validated OTP token [{}] successfully for [{}]", validatedToken, principal);
            validator.store(validatedToken);
            LOGGER.debug("Creating authentication result and building principal for [{}]", principal);
            return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(principal, attributes));
        }
        LOGGER.warn("Authorization of OTP token [{}] has failed", credential);
        throw new FailedLoginException("Failed to authenticate code " + credential);
    }
}
