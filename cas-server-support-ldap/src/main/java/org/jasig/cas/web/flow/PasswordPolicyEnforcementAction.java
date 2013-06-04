/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.LdapAuthenticationException;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.PasswordPolicyEnforcer;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Misagh Moayyed
 * @since 3.5
 */
public final class PasswordPolicyEnforcementAction extends AbstractAction implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PasswordPolicyEnforcer passwordPolicyEnforcer;

    private String passwordPolicyUrl;

    private long redirectTimeout = 10000;

    public PasswordPolicyEnforcer getPasswordPolicyEnforcer() {
        return this.passwordPolicyEnforcer;
    }

    public String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    public void setPasswordPolicyEnforcer(final PasswordPolicyEnforcer enforcer) {
        this.passwordPolicyEnforcer = enforcer;
    }

    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }

    /**
     * @since 4.0
     * @param timeout the number of milliseconds the warning page should
     *                wait before redirecting to the service
     */
    public void setRedirectTimeout(final long timeout) {
        this.redirectTimeout = timeout;
    }

    private void populateErrorsInstance(final LdapAuthenticationException e, final RequestContext reqCtx) {
        try {
            final String code = e.getCode();
            reqCtx.getMessageContext().addMessage(new MessageBuilder().error().code(code).defaultText(code).build());
        } catch (final Exception fe) {
            logger.error(fe.getMessage(), fe);
        }
    }

    private Event warning() {
        return result("showWarning");
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        logger.debug("Checking account status for password...");

        final String ticket = context.getRequestScope().getString("serviceTicketId");
        final UsernamePasswordCredentials credentials = (UsernamePasswordCredentials)
                                                        context.getFlowScope().get("credentials");
        final String userId = credentials.getUsername();
        final String password = credentials.getPassword();

        Event returnedEvent = error();
        String msgToLog = null;

        try {
            if (userId == null && ticket == null) {
                msgToLog = "No user principal or service ticket available.";
                logger.error(msgToLog);
                throw new LdapPasswordPolicyEnforcementException(BadCredentialsAuthenticationException.CODE, msgToLog);
            }

            if (userId == null && ticket != null) {
                returnedEvent = success();
                logger.debug("Received service ticket {} but no user id. Skipping password enforcement.", ticket);
            } else {
                logger.debug("Retrieving number of days to password expiration date for user {}", userId);

                final long daysToExpirationDate = getPasswordPolicyEnforcer()
                                                  .getNumberOfDaysToPasswordExpirationDate(userId, password);

                if (daysToExpirationDate == -1) {
                    returnedEvent = success();
                    logger.debug("Password for {} is not expiring", userId);
                } else {
                    returnedEvent = warning();
                    logger.debug("Password for {} is expiring in {} days", userId, daysToExpirationDate);
                    context.getFlowScope().put("expireDays", daysToExpirationDate);
                    context.getFlowScope().put("redirectTimeout", this.redirectTimeout);
                }
            }
        } catch (final LdapAuthenticationException e) {
            logger.error(e.getMessage(), e);
            populateErrorsInstance(e, context);
            returnedEvent = error();
        } finally {
            logger.debug("Switching to flow event id {} for user {}", returnedEvent.getId(), userId);
        }

        return returnedEvent;
    }

    @Override
    protected void initAction() throws Exception {
        Assert.notNull(getPasswordPolicyEnforcer(), "password policy enforcer cannot be null");
        logger.debug("Initialized the action with password policy enforcer {}",
                getPasswordPolicyEnforcer().getClass().getName());
    }
}
