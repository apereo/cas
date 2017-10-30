package org.apereo.cas.adaptors.authy;

import com.authy.api.Token;
import com.authy.api.User;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Authy authentication handler for CAS.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final boolean forceVerification;
    private final AuthyClientInstance instance;

    public AuthyAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final AuthyClientInstance instance, final boolean forceVerification) {
        super(name, servicesManager, principalFactory, null);
        this.instance = instance;
        this.forceVerification = forceVerification;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final AuthyTokenCredential tokenCredential = (AuthyTokenCredential) credential;
        final RequestContext context = RequestContextHolder.getRequestContext();
        final Principal principal = WebUtils.getAuthentication(context).getPrincipal();

        final User user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(user.getError()));
        }

        final Map<String, String> options = new HashMap<>(1);
        options.put("force", Boolean.toString(this.forceVerification));

        final Token verification = this.instance.getAuthyTokens().verify(user.getId(), tokenCredential.getToken(), options);

        if (!verification.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, new ArrayList<>());
    }
    
    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
