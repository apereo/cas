/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AbstractPasswordWarningCheck;
import org.jasig.cas.authentication.PasswordWarningCheck;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;


/**
 * This action checks the expiration time for a user and displays a warning page when the expiration date is near.  
 * Password Expiration and Account locking are NOT done here -- those are AuthenticationExceptions in 
 * org.jasig.cas.adaptors.ldap.BindLdapAuthenticationHandler
 * 
 * This action should be run after the TGT and ST are created, but before the client is sent back to the service.
 * Based on AccountStatusGetter by Bart Ophelders & Johan Peeters
 * 
 * @author Eric Pierce
 * @version 1.1 3/30/2009 11:47:37
 * 
 */
public final class PasswordWarningCheckAction extends AbstractAction implements InitializingBean {

    private PasswordWarningCheck passwordWarningChecker;
    
    public final PasswordWarningCheck getPasswordWarningCheck() {
        return this.passwordWarningChecker;
    }
    
    public final void setPasswordWarningCheck(final PasswordWarningCheck passwordWarningChecker) {
        this.passwordWarningChecker = passwordWarningChecker;
    }

    protected Event doExecute(final RequestContext context) throws Exception {
        this.logger.debug("checking account status--");
        int status = AbstractPasswordWarningCheck.STATUS_ERROR;

        String ticket = context.getRequestScope().getString("serviceTicketId");
        UsernamePasswordCredentials credentials = (UsernamePasswordCredentials)context.getFlowScope().get("credentials"); 
        String userID=credentials.getUsername();
        this.logger.debug("userID='" + userID + "'");
        
        if ((userID == null)&&(ticket == null)){
        	this.logger.warn("No user principal or service ticket available!");
        	return error();
        }
        
        if ((userID == null) && (ticket != null)){
        	this.logger.debug("Not a login attempt, skipping PasswordWarnCheck");
        	return success();
        }
        
        status = this.passwordWarningChecker.getPasswordWarning(userID);
        this.logger.debug("translating return code status='" + status + "'");
        if (status >= 0) {
            this.logger.info("password for '" + userID + "' is expiring in "+ status + " days. Sending the warning page.");
            context.getFlowScope().put("expireDays", status);
            return Warning();
        }
        if (status == AbstractPasswordWarningCheck.STATUS_PASS) {
            this.logger.info("password for '" + userID + "' is NOT expiring soon.");
        }
        if (status == AbstractPasswordWarningCheck.STATUS_ERROR) {
            this.logger.warn("Error getting expiration date for '" + userID + "'");
        }
        
        return success();
    }
    
    private final Event Warning() {
        return result("showWarning");
    }

    protected void initAction() throws Exception {
        Assert.notNull(this.passwordWarningChecker, "passwordWarningChecker cannot be null");
        this.logger.debug("inited with passwordWarningChecker='"
            + this.passwordWarningChecker.getClass().getName() + "'");
    }
}