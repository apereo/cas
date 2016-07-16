package org.apereo.cas.adaptors.gauth.web.flow;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccount;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAccountSaveRegistrationAction extends AbstractAction {
    private IGoogleAuthenticator googleAuthenticator;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final GoogleAuthenticatorAccount account = requestContext.getFlowScope().get("key", GoogleAuthenticatorAccount.class);

        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        googleAuthenticator.getCredentialRepository().saveUserCredentials(uid, account.getSecretKey(),
                account.getValidationCode(), account.getScratchCodes());
        
        return success();
    }

    public IGoogleAuthenticator getGoogleAuthenticator() {
        return googleAuthenticator;
    }

    public void setGoogleAuthenticator(final IGoogleAuthenticator googleAuthenticator) {
        this.googleAuthenticator = googleAuthenticator;
    }
}
