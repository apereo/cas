package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.commons.lang3.StringUtils;
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
    
    private GoogleAuthenticator googleAuthenticatorInstance;

    /**
     * Instantiates a new Google authenticator authentication handler.
     */
    public GoogleAuthenticatorAuthenticationHandler() {
    }
    

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final GoogleAuthenticatorTokenCredential tokenCredential = (GoogleAuthenticatorTokenCredential) credential;

        final int otp = tokenCredential.getToken();

        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        final String secKey = this.googleAuthenticatorInstance.getCredentialRepository().getSecretKey(uid);
        if (StringUtils.isNotBlank(secKey)) {
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

    public GoogleAuthenticator getGoogleAuthenticatorInstance() {
        return googleAuthenticatorInstance;
    }

    public void setGoogleAuthenticatorInstance(final GoogleAuthenticator googleAuthenticatorInstance) {
        this.googleAuthenticatorInstance = googleAuthenticatorInstance;
    }
}
