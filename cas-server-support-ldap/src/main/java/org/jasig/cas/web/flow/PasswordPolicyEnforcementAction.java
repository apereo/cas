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

import javax.security.auth.login.CredentialException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ErrorMessageResolver;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.PasswordPolicyEnforcer;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Marvin S. Addison
 *
 */
public final class PasswordPolicyEnforcementAction extends AbstractAction {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private PasswordPolicyEnforcer passwordPolicyEnforcer;

    private String passwordPolicyUrl;

    @NotNull
    private ErrorMessageResolver errorMessageResolver;

    public final PasswordPolicyEnforcer getPasswordPolicyEnforcer() {
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

    public void setErrorMessageResolver(final ErrorMessageResolver errorMessageResolver) {
        this.errorMessageResolver = errorMessageResolver;
    }

    private void populateErrorsInstance(final Exception e, final RequestContext reqCtx) {
        try {
            reqCtx.getMessageContext().addMessage(new CasMessageResolver(this.errorMessageResolver.resolve(e)));
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
        final UsernamePasswordCredential credentials = (UsernamePasswordCredential) context.getFlowScope().get("credentials");
        final String userId = credentials.getUsername();

        Event returnedEvent = error();

        try {

            if (userId == null && ticket == null) {
                throw new CredentialException("No user principal or service ticket available.");
            }

            if (userId == null && ticket != null) {
                returnedEvent = success();
                logger.debug("Received service ticket {} but no user id. Skipping password enforcement.", ticket);
            } else {
                logger.debug("Retrieving number of days to password expiration date for user {}", userId);

                final long daysToExpirationDate =
                        getPasswordPolicyEnforcer().getNumberOfDaysToPasswordExpirationDate(userId);
                if (daysToExpirationDate == -1) {
                    returnedEvent = success();
                    this.logger.debug("Password for {} is not expiring", userId);
                } else {
                    returnedEvent = warning();
                    this.logger.debug("Password for {} is expiring in {} days", userId, daysToExpirationDate);
                    context.getFlowScope().put("expireDays", daysToExpirationDate);
                }

            }
        } catch (final LdapPasswordPolicyEnforcementException e) {
            this.logger.error(e.getMessage(), e);
            populateErrorsInstance(e, context);
            returnedEvent = error();
        } finally {
            this.logger.debug("Switching to event id {} for user {}", returnedEvent.getId(), userId);
        }

        return returnedEvent;
    }
}
