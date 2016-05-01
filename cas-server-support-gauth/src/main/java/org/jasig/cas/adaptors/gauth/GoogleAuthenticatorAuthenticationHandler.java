package org.jasig.cas.adaptors.gauth;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.annotation.PostConstruct;
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
@RefreshScope
@Component("googleAuthenticatorAuthenticationHandler")
public class GoogleAuthenticatorAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private GoogleAuthenticatorAccountRegistry accountRegistry;

    @Autowired
    @Qualifier("googleAuthenticatorInstance")
    private GoogleAuthenticatorInstance googleAuthenticatorInstance;

    /**
     * Instantiates a new Google authenticator authentication handler.
     */
    public GoogleAuthenticatorAuthenticationHandler() {
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final GoogleAuthenticatorTokenCredential tokenCredential = (GoogleAuthenticatorTokenCredential) credential;

        final int otp = tokenCredential.getToken();

        final RequestContext context = RequestContextHolder.getRequestContext();
        final String uid = WebUtils.getAuthentication(context).getPrincipal().getId();

        if (!this.accountRegistry.contains(uid)) {
            throw new AccountNotFoundException(uid + " cannot be found in the registry");
        }

        final GoogleAuthenticatorAccount account = this.accountRegistry.get(uid);
        final boolean isCodeValid = this.googleAuthenticatorInstance.authorize(account.getSecretKey(), otp);
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
}
