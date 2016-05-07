package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccount;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccountRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("saveAccountRegistrationAction")
public class GoogleAccountSaveRegistrationAction extends AbstractAction {
    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private GoogleAuthenticatorAccountRegistry accountRegistry;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final GoogleAuthenticatorAccount account = requestContext.getFlowScope().get("key", GoogleAuthenticatorAccount.class);

        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        this.accountRegistry.save(uid, account);
        
        return success();
    }
}
