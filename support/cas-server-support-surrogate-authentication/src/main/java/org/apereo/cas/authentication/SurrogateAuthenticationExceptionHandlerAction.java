package org.apereo.cas.authentication;

import static java.util.Objects.nonNull;

import java.util.Set;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

public class SurrogateAuthenticationExceptionHandlerAction extends AuthenticationExceptionHandlerAction {

    public SurrogateAuthenticationExceptionHandlerAction(final Set<Class<? extends Throwable>> errors, String messageBundlePrefix) {
        super(errors, messageBundlePrefix);
    }

    @Override
    protected String handleAuthenticationException(final AuthenticationException e, final RequestContext requestContext) {
        String handlerErrorName = super.handleAuthenticationException(e, requestContext);
        revertSurrogateCredential(requestContext);
        return handlerErrorName;
    }

    private void revertSurrogateCredential(final RequestContext requestContext) {
        Credential credential = WebUtils.getCredential(requestContext);
        if (nonNull(credential) && credential instanceof SurrogateUsernamePasswordCredential) {
            SurrogateUsernamePasswordCredential surrogateCred = (SurrogateUsernamePasswordCredential) credential;
            RememberMeUsernamePasswordCredential revertedCred = new RememberMeUsernamePasswordCredential();
            revertedCred.setUsername(surrogateCred.getUsername());
            revertedCred.setPassword(surrogateCred.getPassword());
            revertedCred.setRememberMe(surrogateCred.isRememberMe());
            WebUtils.putCredential(requestContext, revertedCred);
        }
    }
}
