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
package org.jasig.cas.adaptors.ldap.lppe.web.flow;

import org.jasig.cas.adaptors.ldap.LdapAuthenticationException;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyAuthenticationException;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyAwareAuthenticationHandler;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.flow.AuthenticationViaFormAction;

import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

public class LdapPasswordPolicyAwareAuthenticationViaFormAction extends AuthenticationViaFormAction {

    private LdapPasswordPolicyAwareAuthenticationHandler ldapPasswordPolicyAuthenticationHandler;
    
    public void setLdapPasswordPolicyAuthenticationHandler(final LdapPasswordPolicyAwareAuthenticationHandler ldapPasswordPolicyAuthenticationHandler) {
        this.ldapPasswordPolicyAuthenticationHandler = ldapPasswordPolicyAuthenticationHandler;
    }

    @Override
    protected String getAuthenticationWebFlowErrorEventId(final RequestContext context, final Credentials credentials, 
                                                          final MessageContext messageContext, final TicketException e) {
        
        String eventId = super.getAuthenticationWebFlowErrorEventId(context, credentials, messageContext, e); 
        
        if (isTicketExceptionCauseAuthenticationException(e)) {
            final AuthenticationException ex = (AuthenticationException) e.getCause();
            log.debug("Handling ldap password policy authentication error...");
                        
            if (LdapAuthenticationException.class.isAssignableFrom(ex.getClass())) {
               eventId = ex.getType(); 
            }   
        } 
        
        log.debug("Returning webflow error event id: {}", eventId);
        return eventId;
    }
    
    @Override
    protected String getAuthenticationWebFlowSuccessEventId(RequestContext context, Credentials credentials, MessageContext messageContext) {
        if (isLdapPasswordPolicyAuthenticationHandlerUsed()) {
            try {
                this.ldapPasswordPolicyAuthenticationHandler.validateAccountPasswordExpirationPolicy();
            }  catch (final LdapPasswordPolicyAuthenticationException e) {
                context.getFlowScope().put("expireDays", e.getNumberOfDaysToPasswordExpirationDate());
                return ((AuthenticationException)e.getCause()).getType();
            }
        }
        
        return super.getAuthenticationWebFlowSuccessEventId(context, credentials, messageContext);
    }

    private boolean isLdapPasswordPolicyAuthenticationHandlerUsed() {
        return (this.ldapPasswordPolicyAuthenticationHandler == null || this.ldapPasswordPolicyAuthenticationHandler.getPasswordPolicyConfiguration() != null);
    }
}
