package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * An authentication handler that uses the token provided
 * to authenticator against google authN for MFA.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAuthenticatorAuthenticationHandler.class);
    
    private final IGoogleAuthenticator googleAuthenticatorInstance;
    private final OneTimeTokenRepository tokenRepository;
    private final OneTimeTokenCredentialRepository credentialRepository;

    public GoogleAuthenticatorAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                    final IGoogleAuthenticator googleAuthenticatorInstance, final OneTimeTokenRepository tokenRepository,
                                                    final OneTimeTokenCredentialRepository credentialRepository) {
        super(name, servicesManager, principalFactory, null);
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
        this.tokenRepository = tokenRepository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final GoogleAuthenticatorTokenCredential tokenCredential = (GoogleAuthenticatorTokenCredential) credential;

        if (!StringUtils.isNumeric(tokenCredential.getToken())) {
            throw new PreventedException("Invalid non-numeric OTP format specified.",
                    new IllegalArgumentException("Invalid token " + tokenCredential.getToken()));
        }
        final int otp = Integer.parseInt(tokenCredential.getToken());
        LOGGER.debug("Received OTP [{}]", otp);

        final RequestContext context = RequestContextHolder.getRequestContext();
        if (context == null) {
            throw new IllegalArgumentException("No request context could be found to locate an authentication event");
        }
        final Authentication authentication = WebUtils.getAuthentication(context);
        if (authentication == null) {
            throw new IllegalArgumentException("Request context has no reference to an authentication event to locate a principal");
        }
        final String uid = authentication.getPrincipal().getId();
        LOGGER.debug("Received principal id [{}]", uid);

        LOGGER.debug("Locating principal id [{}] in credential repository", uid);
        final OneTimeTokenAccount acct = this.credentialRepository.get(uid);
        if (acct == null || StringUtils.isBlank(acct.getSecretKey())) {
            throw new AccountNotFoundException(uid + " cannot be found in the registry");
        }

        LOGGER.debug("Locating OTP [{}] for principal [{}] in token repository", otp, uid);
        if (this.tokenRepository.exists(uid, otp)) {
            throw new AccountExpiredException(uid + " cannot reuse OTP " + otp + " as it may be expired/invalid");
        }

        LOGGER.debug("Attempting to authorize OTP [{}]", otp);
        final boolean isCodeValid = this.googleAuthenticatorInstance.authorize(acct.getSecretKey(), otp);
        if (isCodeValid) {
            this.tokenRepository.store(new GoogleAuthenticatorToken(otp, uid));
            return createHandlerResult(tokenCredential, this.principalFactory.createPrincipal(uid), null);
        }
        LOGGER.warn("Token authorization for OTP [{}] has failed", otp);
        throw new FailedLoginException("Failed to authenticate code " + otp);
    }

    @Override
    public boolean supports(final Credential credential) {
        return GoogleAuthenticatorTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
