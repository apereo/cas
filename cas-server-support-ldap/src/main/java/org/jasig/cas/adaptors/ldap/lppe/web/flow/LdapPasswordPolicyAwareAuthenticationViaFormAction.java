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
import org.jasig.cas.web.flow.AuthenticationViaFormAction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.message.MessageContext;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * An extension of the {@link AuthenticationViaFormAction} whose main task is to communicate the ldap authentication error
 * type back to the flow in case of an error, or invoke policy examiners after authentication has taken place successfully.
 * This action simplifies extending the authentication flow by allowing a mapping between authentication error types
 * and the event id to which the flow may switch.  
 * 
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class LdapPasswordPolicyAwareAuthenticationViaFormAction extends AuthenticationViaFormAction implements InitializingBean {

    private LdapPasswordPolicyAwareAuthenticationHandler ldapPasswordPolicyAuthenticationHandler;
    
    public void setLdapPasswordPolicyAuthenticationHandler(final LdapPasswordPolicyAwareAuthenticationHandler ldapPasswordPolicyAuthenticationHandler) {
        this.ldapPasswordPolicyAuthenticationHandler = ldapPasswordPolicyAuthenticationHandler;
    }
    
    @Override
    protected Event getAuthenticationWebFlowErrorEventId(final RequestContext context, final Credentials credentials, 
                                                         final MessageContext messageContext, final Exception e) {
        
        Event eventId = super.getAuthenticationWebFlowErrorEventId(context, credentials, messageContext, e); 
        
        if (isExceptionCauseAuthenticationException(e)) {
            final AuthenticationException ex = (AuthenticationException) e.getCause();
            log.debug("Handling ldap password policy authentication error...");
                        
            if (LdapAuthenticationException.class.isAssignableFrom(ex.getClass())) {
               eventId = new Event(this, ex.getType()); 
            }   
        } 
        
        log.debug("Returning webflow error event id: {}", eventId);
        return eventId;
    }
    
    @Override
    protected Event getAuthenticationWebFlowSuccessEventId(final RequestContext context, final Credentials credentials, 
                                                           final MessageContext messageContext) {
        Event eventId = super.getAuthenticationWebFlowSuccessEventId(context, credentials, messageContext);
        
        try {
            
            if (isLdapPasswordPolicyAuthenticationSuccessful(credentials)) {
                final LdapPasswordPolicyConfiguration configuration = this.ldapPasswordPolicyAuthenticationHandler.getPasswordPolicyConfiguration();
                final List<LdapPasswordPolicyExaminer> examinersList = this.ldapPasswordPolicyAuthenticationHandler.getLdapPasswordPolicyExaminers();
                
                if (examinersList != null && examinersList.size() > 0) {
                  log.debug("Ldap password policy authentication has passed. Invoking ldap password policy examiners...");
                  for (final LdapPasswordPolicyExaminer examiner : this.ldapPasswordPolicyAuthenticationHandler.getLdapPasswordPolicyExaminers()) {
                      examiner.examinePasswordPolicy(configuration);
                  }
                }
            } else {
                log.debug("Ldap password policy authentication has failed. This may be due to account password policy, " +
                		      "or the provided credentials cannot be located in the configured ldap.");
            }
        }  catch (final LdapPasswordPolicyExpirationException e) {
            context.getFlowScope().put("expireDays", e.getNumberOfDaysToPasswordExpirationDate());
            eventId = new Event(this, e.getType());
        } catch (final LdapPasswordPolicyAuthenticationException e) {
            eventId = new Event(this, e.getType());
        } catch (final AuthenticationException e) {
            populateErrorsInstance(e.getCode(), messageContext);
            eventId = getAuthenticationWebFlowErrorEventId(context, credentials, messageContext,e);
        }
         
        
        return eventId;
    }

    private boolean isLdapPasswordPolicyAuthenticationSuccessful(final Credentials credentials) throws AuthenticationException {
        if (this.ldapPasswordPolicyAuthenticationHandler.getPasswordPolicyConfiguration() == null) {
            return (this.ldapPasswordPolicyAuthenticationHandler.authenticate(credentials));
        } 
        
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapPasswordPolicyAuthenticationHandler, "ldapPasswordPolicyAuthenticationHandler cannot be null");       
    }
}
