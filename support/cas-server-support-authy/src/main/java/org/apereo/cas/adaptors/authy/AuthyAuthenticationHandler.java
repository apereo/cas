package org.apereo.cas.adaptors.authy;

import com.authy.api.Token;
import com.authy.api.User;
import com.google.common.collect.Lists;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authy authentication handler for CAS.
 *
 * @author Misagh Moayyed
 * @since 5.0
 */
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private Boolean forceVerification = Boolean.FALSE;
    private final AuthyClientInstance instance;

    public AuthyAuthenticationHandler(final AuthyClientInstance instance) {
        this.instance = instance;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final AuthyTokenCredential tokenCredential = (AuthyTokenCredential) credential;
        final RequestContext context = RequestContextHolder.getRequestContext();
        final Principal principal = WebUtils.getAuthentication(context).getPrincipal();

        final User user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(user.getError()));
        }
        final Integer authyId = user.getId();

        final Map<String, String> options = new HashMap<>();
        options.put("force", this.forceVerification.toString());

        final Token verification = this.instance.getAuthyTokens().verify(authyId, tokenCredential.getToken(), options);

        if (!verification.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, Lists.newArrayList());
    }
    
    public void setForceVerification(final Boolean forceVerification) {
        this.forceVerification = forceVerification;
    }
    
    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
