/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.LdapAuthenticationException;
import org.jasig.cas.authentication.LdapPasswordEnforcementException;
import org.jasig.cas.authentication.PasswordWarningCheck;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.message.MessageBuilder;
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
        return passwordWarningChecker;
    }

    public void setPasswordWarningCheck(final PasswordWarningCheck passwordWarningChecker) {
        this.passwordWarningChecker = passwordWarningChecker;
    }

    private void populateErrorsInstance(final LdapAuthenticationException e, final RequestContext reqCtx) {
        try {
            final String code = e.getCode();
            reqCtx.getMessageContext().addMessage(new MessageBuilder().error().code(code).defaultText(code).build());
        } catch (final Exception fe) {
            logger.error(fe.getMessage(), fe);
        }
    }

    private final Event warning() {
        return result("showWarning");
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        logger.debug("Checking account status for password...");

        final String ticket = context.getRequestScope().getString("serviceTicketId");
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) context.getFlowScope().get("credentials");
        final String userId = credentials.getUsername();

        Event returnedEvent = error();

        String msgToLog = null;

        try {

            if (userId == null && ticket == null) {
                msgToLog = "No user principal or service ticket available.";
                logger.warn(msgToLog);
                throw new LdapPasswordEnforcementException(BadCredentialsAuthenticationException.CODE, msgToLog);
            }

            if (userId == null && ticket != null) {
                logger.debug("Received service ticket " + ticket
                        + " but no user id. This is not a login attempt, so skip the password check...");
                returnedEvent = success();
            } else {
                logger.debug("Retrieving number of days to password expiration date for user " + userId + "...");

                final int daysToExpirationDate = passwordWarningChecker.getNumberOfDaysToPasswordExpirationDate(userId);

                switch (daysToExpirationDate) {
                case -1:
                    logger.info("Password for " + userId + " is not expiring. Switching the flow to success");
                    break;
                default:
                    logger.info("Password for " + userId + " is expiring in " + daysToExpirationDate + " days. Switching the flow to warn");
                    context.getFlowScope().put("expireDays", daysToExpirationDate);
                    returnedEvent = warning();
                    break;
                }

            }
        } catch (final LdapAuthenticationException e) {
            logger.error("Switching the flow to error for " + userId + ". " + e.getMessage(), e);
            populateErrorsInstance(e, context);
            returnedEvent = error();
        }

        return returnedEvent;
    }

    @Override
    protected void initAction() throws Exception {
        Assert.notNull(passwordWarningChecker, "passwordWarningChecker cannot be null");
        logger.debug("Initialized the action with passwordWarningChecker='" + passwordWarningChecker.getClass().getName() + "'");
    }

}