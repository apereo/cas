/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.flow;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdSingleSignOnAction extends AbstractNonInteractiveCredentialsAction {

    @NotNull
    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();
    
    public void setExtractor(final OpenIdUserNameExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Credentials constructCredentialsFromRequest(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String userName = this.extractor
            .extractLocalUsernameFromUri(context.getRequestParameters()
                .get("openid.identity"));
        final Service service = WebUtils.getService(context);
        
        context.getExternalContext().getSessionMap().put("openIdLocalId", userName);
        
        // clear the service because otherwise we can fake the username
        if (service instanceof OpenIdService && userName == null) {
            context.getFlowScope().remove("service");
        }
        
        if (ticketGrantingTicketId == null || userName == null) {
            return null;
        }
        
        return new OpenIdCredentials(
            ticketGrantingTicketId, userName);
    }
}
