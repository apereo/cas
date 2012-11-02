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

import java.util.List;

import org.jasig.cas.adaptors.ldap.LdapAuthenticationException;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyAuthenticationException;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyAwareAuthenticationHandler;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyConfiguration;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyExaminer;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyExpirationException;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.flow.AuthenticationViaFormAction;

import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * An extension of the {@link AuthenticationViaFormAction} whose main task is to communicate the ldap authentication error
 * type back to the flow in case of an error, or invoke policy examiners after authentication has taken place successfully.
 * This action simplifies extending the authentication flow by allowing a mapping between authentication error types
 * and the event id to which the flow may switch.  
 */
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
        String eventId = super.getAuthenticationWebFlowSuccessEventId(context, credentials, messageContext);
        
        if (isLdapPasswordPolicyAuthenticationHandlerUsed()) {
            try {
                final LdapPasswordPolicyConfiguration configuration = this.ldapPasswordPolicyAuthenticationHandler.getPasswordPolicyConfiguration();
                final List<LdapPasswordPolicyExaminer> examinersList = this.ldapPasswordPolicyAuthenticationHandler.getLdapPasswordPolicyExaminers();
                
                if (examinersList != null && examinersList.size() > 0) {
                  for (final LdapPasswordPolicyExaminer examiner : this.ldapPasswordPolicyAuthenticationHandler.getLdapPasswordPolicyExaminers()) {
                      examiner.examinePasswordPolicy(configuration);
                  }
                }
            }  catch (final LdapPasswordPolicyExpirationException e) {
                context.getFlowScope().put("expireDays", e.getNumberOfDaysToPasswordExpirationDate());
                eventId = e.getType();
            } catch (final LdapPasswordPolicyAuthenticationException e) {
                eventId = e.getType();
            }
        }
        
        return eventId;
    }

    private boolean isLdapPasswordPolicyAuthenticationHandlerUsed() {
        return (this.ldapPasswordPolicyAuthenticationHandler == null || this.ldapPasswordPolicyAuthenticationHandler.getPasswordPolicyConfiguration() != null);
    }
}
