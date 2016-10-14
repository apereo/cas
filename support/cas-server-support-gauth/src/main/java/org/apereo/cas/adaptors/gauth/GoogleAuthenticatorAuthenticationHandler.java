package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

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
    
    private IGoogleAuthenticator googleAuthenticatorInstance;

    /**
     * Instantiates a new Google authenticator authentication handler.
     */
    public GoogleAuthenticatorAuthenticationHandler() {
    }
    
    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final GoogleAuthenticatorTokenCredential tokenCredential = (GoogleAuthenticatorTokenCredential) credential;

        if (!NumberUtils.isNumber(tokenCredential.getToken())) {
            throw new PreventedException("Invalid non-numeric OTP format specified.", new IllegalArgumentException());
        }
        final int otp = Integer.parseInt(tokenCredential.getToken());
        logger.debug("Received OTP {}", otp);
        
        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        logger.debug("Received principal id {}", uid);
        
        final String secKey = this.googleAuthenticatorInstance.getCredentialRepository().getSecretKey(uid);
        if (StringUtils.isBlank(secKey)) {
            throw new AccountNotFoundException(uid + " cannot be found in the registry");
        }
        
        final boolean isCodeValid = this.googleAuthenticatorInstance.authorize(secKey, otp);
        if (isCodeValid) {
            return createHandlerResult(tokenCredential,
                    this.principalFactory.createPrincipal(uid), null);
        }
        throw new FailedLoginException("Failed to authenticate code " + otp);
    }

    @Override
    public boolean supports(final Credential credential) {
        return GoogleAuthenticatorTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    public IGoogleAuthenticator getGoogleAuthenticatorInstance() {
        return googleAuthenticatorInstance;
    }

    public void setGoogleAuthenticatorInstance(final IGoogleAuthenticator googleAuthenticatorInstance) {
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
    }
}
