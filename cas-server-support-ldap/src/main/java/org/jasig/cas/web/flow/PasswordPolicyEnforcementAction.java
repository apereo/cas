/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.LdapAuthenticationException;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.PasswordPolicyEnforcer;
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
public final class PasswordPolicyEnforcementAction extends AbstractAction implements InitializingBean {

    private PasswordPolicyEnforcer passwordPolicyEnforcer;

    public final PasswordPolicyEnforcer getPasswordPolicyEnforcer() {
        return passwordPolicyEnforcer;
    }

    public void setPasswordPolicyEnforcer(final PasswordPolicyEnforcer enforcer) {
        passwordPolicyEnforcer = enforcer;
    }

    private void populateErrorsInstance(final LdapAuthenticationException e, final RequestContext reqCtx) {
        try {
            final String code = e.getCode();
            reqCtx.getMessageContext().addMessage(new MessageBuilder().error().code(code).defaultText(code).build());
        } catch (final Exception fe) {

            if (logger.isErrorEnabled())
                logger.error(fe.getMessage(), fe);

        }
    }

    private final Event warning() {
        return result("showWarning");
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        if (logger.isDebugEnabled())
            logger.debug("Checking account status for password...");

        final String ticket = context.getRequestScope().getString("serviceTicketId");
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) context.getFlowScope().get("credentials");
        final String userId = credentials.getUsername();

        Event returnedEvent = error();

        String msgToLog = null;

        try {

            if (userId == null && ticket == null) {
                msgToLog = "No user principal or service ticket available.";

                if (logger.isErrorEnabled())
                    logger.error(msgToLog);

                throw new LdapPasswordPolicyEnforcementException(BadCredentialsAuthenticationException.CODE, msgToLog);
            }

            if (userId == null && ticket != null) {

                returnedEvent = success();

                if (logger.isDebugEnabled())
                    logger.debug("Received service ticket " + ticket + " but no user id. This is not a login attempt, so skip password enforcement.");

            } else {

                if (logger.isDebugEnabled())
                    logger.debug("Retrieving number of days to password expiration date for user " + userId);

                final int daysToExpirationDate = getPasswordPolicyEnforcer().getNumberOfDaysToPasswordExpirationDate(userId);

                switch (daysToExpirationDate) {
                case -1:

                    returnedEvent = success();

                    if (logger.isDebugEnabled())
                        logger.debug("Password for " + userId + " is not expiring");

                    break;
                default:
                    returnedEvent = warning();

                    if (logger.isDebugEnabled())
                        logger.debug("Password for " + userId + " is expiring in " + daysToExpirationDate + " days");

                    context.getFlowScope().put("expireDays", daysToExpirationDate);

                    break;
                }

            }
        } catch (final LdapAuthenticationException e) {
            if (logger.isErrorEnabled())
                logger.error(e.getMessage(), e);

            populateErrorsInstance(e, context);
            returnedEvent = error();
        } finally {

            if (logger.isDebugEnabled())
                logger.debug("Switching to flow event id " + returnedEvent.getId() + " for user " + userId);
        }

        return returnedEvent;
    }

    @Override
    protected void initAction() throws Exception {
        Assert.notNull(getPasswordPolicyEnforcer(), "password policy enforcer cannot be null");

        if (logger.isDebugEnabled())
            logger.debug("Initialized the action with password policy enforcer " + getPasswordPolicyEnforcer().getClass().getName());
    }
}
