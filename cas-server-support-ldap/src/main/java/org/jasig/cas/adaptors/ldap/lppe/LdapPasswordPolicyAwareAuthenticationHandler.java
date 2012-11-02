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
package org.jasig.cas.adaptors.ldap.lppe;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jasig.cas.adaptors.ldap.AbstractLdapUsernamePasswordAuthenticationHandler;
import org.jasig.cas.adaptors.ldap.LdapAuthenticationException;
import org.jasig.cas.adaptors.ldap.lppe.LdapPasswordPolicyExaminer.ActiveDirectoryUserAccountControlFlags;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * An implementation of an ldap authentication handler that acts as the wrapper/decorator around an existing ldap authentication handler.
 * The main task of this handler is configure the attribute retrieval policy for the true authentication handler, retrieve the results,
 * construct an instance of {@link LdapPasswordPolicyConfiguration} that is to be used for examining account policy. 
 * 
 * @see #setLdapAuthenticationHandler(AbstractLdapUsernamePasswordAuthenticationHandler)
 */
public class LdapPasswordPolicyAwareAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {
    
    private LdapPasswordPolicyConfiguration passwordPolicyConfiguration = null;
    
    /** The custom attribute that indicates the account is disabled **/
    private String accountDisabledAttributeName = null;

    /** The custom attribute that indicates the account is locked **/
    private String accountLockedAttributeName = null;

    /** The custom attribute that indicates the account password must change **/
    private String accountPasswordMustChangeAttributeName = null;

    /** The attribute that indicates the user account status **/
    private String userAccountControlAttributeName = "userAccountControl";
        
    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String ignorePasswordExpirationWarningAttributeName = null;

    /** Default number of days which the password may be considered valid **/
    private int defaultValidPasswordNumberOfDays = 90;
    
    /** Default number of days to use when calculating the warning period **/
    private int defaultPasswordWarningNumberOfDays = 30;
    
    /** Url to the password policy application **/
    private String passwordPolicyUrl;
    
    @NotNull
    private AbstractLdapUsernamePasswordAuthenticationHandler ldapAuthenticationHandler = null;
    
    /** 
     * List of error definitions and their types, based on which the user will be directed to a given view in the flow
     * @see LdapErrorDefinition 
     **/
    private List<LdapErrorDefinition> ldapErrorDefinitions = null;

    private List<LdapPasswordPolicyExaminer> ldapPasswordPolicyExaminers = null;
    
    /** The attribute that contains the date the password will expire or last password change */
    private String passwordExpirationDateAttributeName;

    /** The attribute that contains the user's warning days */
    private String passwordWarningNumberOfDaysAttributeName = null;

    /** The attribute that contains the number of days the user's password is valid */
    private String validPasswordNumberOfDaysAttributeName = null;

    public final String getPasswordPolicyUrl() {
        return this.passwordPolicyUrl;
    }

    public void setPasswordPolicyUrl(final String passwordPolicyUrl) {
        this.passwordPolicyUrl = passwordPolicyUrl;
    }
    
    public void setAccountDisabledAttributeName(final String accountDisabledAttributeName) {
        this.accountDisabledAttributeName = accountDisabledAttributeName;
    }

    public void setAccountLockedAttributeName(final String accountLockedAttributeName) {
        this.accountLockedAttributeName = accountLockedAttributeName;
    }

    public void setAccountPasswordMustChangeAttributeName(final String accountPasswordMustChange) {
        this.accountPasswordMustChangeAttributeName = accountPasswordMustChange;
    } 

    public void setIgnorePasswordExpirationWarningAttributeName(final String noWarningAttributeName) {
        this.ignorePasswordExpirationWarningAttributeName = noWarningAttributeName;
    }

    @Required
    public void setLdapAuthenticationHandler(final AbstractLdapUsernamePasswordAuthenticationHandler ldapAuthenticationHandler) {
        this.ldapAuthenticationHandler = ldapAuthenticationHandler;      
    }

    public void setUserAccountControlAttributeName(final String attr) {
        this.userAccountControlAttributeName = attr;
    }
        
    /**
     * Allows to specify various types of {@link LdapErrorDefinition} that are set to capture ldap error codes
     * when binding to the instance. 
     * 
     * @param ldapErrorDefs
     * @see AbstractLdapErrorDefinition
     */
    public void setLdapErrorDefinitions(final List<LdapErrorDefinition> ldapErrorDefs) {
        this.ldapErrorDefinitions = ldapErrorDefs;
    }

    public void setPasswordExpirationDateAttributeName(final String dateAttributeName) {
        this.passwordExpirationDateAttributeName = dateAttributeName;
    }

    public void setValidPasswordNumberOfDaysAttributeName(final String validDaysAttributeName) {
        this.validPasswordNumberOfDaysAttributeName = validDaysAttributeName;
    }

    public void setPasswordWarningNumberOfDaysAttributeName(final String warningDaysAttributeName) {
        this.passwordWarningNumberOfDaysAttributeName = warningDaysAttributeName;
    }

    public void setDefaultValidPasswordNumberOfDays(final int days) {
        this.defaultValidPasswordNumberOfDays = days;
    }
    
    public void setDefaultPasswordWarningNumberOfDays(final int days) {
        this.defaultPasswordWarningNumberOfDays = days;
    }
      
    public LdapPasswordPolicyConfiguration getPasswordPolicyConfiguration() {
        return this.passwordPolicyConfiguration;
    }
       
    public List<LdapPasswordPolicyExaminer> getLdapPasswordPolicyExaminers() {
      return this.ldapPasswordPolicyExaminers;
    }
  
    /**
     * Allows to set a list of {@link LdapPasswordPolicyExaminer}s which may be invoked after authentication has taken place.
     * This is specifically used by the authentication flow to verify account policy condition that may not necessarily
     * be considered errors to prevent authentication, such as password expiration warnings, etc.
     * @param ldapPasswordPolicyExaminers
     */
    public void setLdapPasswordPolicyExaminers(final List<LdapPasswordPolicyExaminer> ldapPasswordPolicyExaminers) {
        this.ldapPasswordPolicyExaminers = ldapPasswordPolicyExaminers;
    }
    
    @Override
    protected boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {
        try {
            final boolean authenticated = this.ldapAuthenticationHandler.authenticate(credentials);
            if (authenticated) {
                
                final SearchResult authenticatedDn = this.ldapAuthenticationHandler.getAuthenticatedDistinguishedNameSearchResult();
                if (authenticatedDn == null) {
                    log.warn("Authentication handler {} has indicated success, but the ldap authentication search result cannot be found. Ignoring password policy checks...",
                            this.ldapAuthenticationHandler.getName());
                    return authenticated;
                }

                this.passwordPolicyConfiguration = buildPasswordPolicyConfiguration(authenticatedDn, credentials);
                
                if (passwordPolicyConfiguration == null) {
                    log.warn("Password policy configuration could not be constructed. Skipping all password policy checks...");
                    return authenticated;
                }
                
                examineAccountStatus(credentials);
            }
            return authenticated;
        } catch (final AuthenticationException e) {
            throw this.handleLdapError(e);
        }
    }

    protected void examineAccountStatus(final UsernamePasswordCredentials credential) throws AuthenticationException {
        final long uacValue = getPasswordPolicyConfiguration().getUserAccountControl();
        final String uid =  getPasswordPolicyConfiguration().getCredentials().getUsername();
        
        if (uacValue > 0) {
           
           if ((uacValue & ActiveDirectoryUserAccountControlFlags.UAC_FLAG_ACCOUNT_DISABLED.getValue()) == 
               ActiveDirectoryUserAccountControlFlags.UAC_FLAG_ACCOUNT_DISABLED.getValue()) {
               final String msg = String.format("User account control flag is set. Account %s is disabled", uid);
               throw new AccountDisabledLdapErrorDefinition().getAuthenticationException(msg);
           } 
           
           if ((uacValue & ActiveDirectoryUserAccountControlFlags.UAC_FLAG_LOCKOUT.getValue()) == 
               ActiveDirectoryUserAccountControlFlags.UAC_FLAG_LOCKOUT.getValue()) {
               final String msg = String.format("User account control flag is set. Account %s is locked", uid);
               throw new AccountLockedLdapErrorDefinition().getAuthenticationException(msg);
           } 
           
           if ((uacValue & ActiveDirectoryUserAccountControlFlags.UAC_FLAG_PASSWORD_EXPIRED.getValue()) == 
               ActiveDirectoryUserAccountControlFlags.UAC_FLAG_PASSWORD_EXPIRED.getValue()) {
               
               final String msg = String.format("User account control flag is set. Account %s has expired", uid);
               throw new AccountPasswordExpiredLdapErrorDefinition().getAuthenticationException(msg);
           } 
        }

        if (getPasswordPolicyConfiguration().isAccountDisabled()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is disabled", this.accountDisabledAttributeName, uid);
            throw new AccountDisabledLdapErrorDefinition().getAuthenticationException(msg);
        }
        
        if (getPasswordPolicyConfiguration().isAccountLocked()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s is locked", this.accountLockedAttributeName, uid);
            throw new AccountLockedLdapErrorDefinition().getAuthenticationException(msg);
        }
        
        if (getPasswordPolicyConfiguration().isAccountPasswordMustChange()) {
            final String msg = String.format("Password policy attribute %s is set. Account %s must change it password", 
                                             this.accountPasswordMustChangeAttributeName, uid);
            throw new AccountMustChangePasswordLdapErrorDefinition().getAuthenticationException(msg); 
        }
    }

    private String getPasswordPolicyAttributeValue(final Attributes attrs, final String attrName) throws Exception {
        if (attrName != null) {
            if (attrs.get(attrName) != null) {
                return (String) attrs.get(attrName).get();
            }
        }
        return null;
    }
    
    protected LdapPasswordPolicyConfiguration buildPasswordPolicyConfiguration(final SearchResult authenticatedDn, final UsernamePasswordCredentials credentials) 
                                                                               throws LdapAuthenticationException {
        LdapPasswordPolicyConfiguration configuration = null;
        try {

            final Attributes attrs = authenticatedDn.getAttributes();
            final String expirationDate = getPasswordPolicyAttributeValue(attrs, this.passwordExpirationDateAttributeName);

            if (expirationDate == null) {
                log.warn("Password policy configuration cannot be determined because expiration date does not contain a value for attribute {}", this.passwordExpirationDateAttributeName);
                return null;
            }
             
            configuration = new LdapPasswordPolicyConfiguration(credentials);
            configuration.setPasswordExpirationDateAttributeName(this.passwordExpirationDateAttributeName);
            configuration.setPasswordExpirationDate(expirationDate);
            configuration.setPasswordWarningNumberOfDays(this.defaultPasswordWarningNumberOfDays);
            configuration.setValidPasswordNumberOfDays(this.defaultValidPasswordNumberOfDays);
            
            String attributeValue = getPasswordPolicyAttributeValue(attrs, this.passwordWarningNumberOfDaysAttributeName);
            if (attributeValue != null) {
                if (NumberUtils.isNumber(attributeValue)) {
                    configuration.setPasswordWarningNumberOfDays(Integer.parseInt(attributeValue));
                }
            }

            attributeValue = getPasswordPolicyAttributeValue(attrs, this.ignorePasswordExpirationWarningAttributeName);
            if (attributeValue != null) {
                configuration.setIgnorePasswordExpirationWarning(attributeValue);
            }
            
            attributeValue = getPasswordPolicyAttributeValue(attrs, this.validPasswordNumberOfDaysAttributeName);
            if (attributeValue != null) {
                if (NumberUtils.isNumber(attributeValue)) {
                    configuration.setValidPasswordNumberOfDays(Integer.parseInt(attributeValue));
                }
            }

            attributeValue = getPasswordPolicyAttributeValue(attrs, this.accountDisabledAttributeName);
            if (attributeValue != null) {
                configuration.setAccountDisabled(Boolean.valueOf(attributeValue));
            }
            
            attributeValue = getPasswordPolicyAttributeValue(attrs, this.accountLockedAttributeName);
            if (attributeValue != null) {
                configuration.setAccountLocked(Boolean.valueOf(attributeValue));
            }
            
            attributeValue = getPasswordPolicyAttributeValue(attrs, this.accountPasswordMustChangeAttributeName);
            if (attributeValue != null) {
                configuration.setAccountPasswordMustChange(Boolean.valueOf(attributeValue));
            }

            attributeValue = getPasswordPolicyAttributeValue(attrs, this.userAccountControlAttributeName);
            if (attributeValue != null) {
                configuration.setUserAccountControl(attributeValue);
            }
        } catch (final Exception e) {
            throw new LdapAuthenticationException(e);
        }

        return configuration;
    }

    /**
     * Attempts to handle ldap errors based on the available definitions defined at {@link #setLdapErrorDefinitions()}
     * If a matching definition is found, an instance of {@link LdapAuthenticationException} is thrown with the definition data.
     * Otherwise, an instance of {@link BadCredentialsAuthenticationException} will be thrown. 
     * 
     * @param e The ldap exception that occurred.
     * @return An instance of {@link BadCredentialsAuthenticationException} if an error was left unhandled by the error definitions
     * or an instance of {@link LdapAuthenticationException} that indicates extra info about how the error definition must be handled. 
     */
    protected AuthenticationException handleLdapError(final Exception e) {
        if (this.ldapErrorDefinitions == null || this.ldapErrorDefinitions.size() == 0) {
            log.debug("No error definitions are defined. Throwing error {}", e.getMessage());
            return new LdapAuthenticationException(e);
        }

        log.debug("Handling error {}", e.getMessage());
        for (final LdapErrorDefinition ldapErrorDef : this.ldapErrorDefinitions) {
            if (ldapErrorDef.matches(e.getMessage())) {
                log.debug("Found ldap error definition {}. Throwing error for {}", ldapErrorDef, e.getMessage());
                return ldapErrorDef.getAuthenticationException(e.getMessage());
            }
        }

        log.debug("No error definition could be matched against the error. Throwing default error for {}", e.getMessage());
        return new LdapAuthenticationException(e);
    }

    

    @Override
    public void afterPropertiesSet() throws Exception {
        final List<String> list = new ArrayList<String>(this.ldapAuthenticationHandler.getAttributesToReturn());
        
        if (!StringUtils.isBlank(this.userAccountControlAttributeName)) {
            list.add(this.userAccountControlAttributeName);
        }
        
        if (!StringUtils.isBlank(this.accountDisabledAttributeName)) {
            list.add(this.accountDisabledAttributeName);
        }
        
        if (!StringUtils.isBlank(this.accountLockedAttributeName)) {
            list.add(this.accountLockedAttributeName);
        }
        
        if (!StringUtils.isBlank(this.accountPasswordMustChangeAttributeName)) {
            list.add(this.accountPasswordMustChangeAttributeName);
        }
        
        if (!StringUtils.isBlank(this.ignorePasswordExpirationWarningAttributeName)) {
            list.add(this.ignorePasswordExpirationWarningAttributeName);
        }
        
        if (!StringUtils.isBlank(this.passwordExpirationDateAttributeName)) {
            list.add(this.passwordExpirationDateAttributeName);
        }
        
        if (!StringUtils.isBlank(this.passwordWarningNumberOfDaysAttributeName)) {
            list.add(this.passwordWarningNumberOfDaysAttributeName);
        }
        
        if (!StringUtils.isBlank(this.validPasswordNumberOfDaysAttributeName)) {
            list.add(this.validPasswordNumberOfDaysAttributeName);
        }
        
        this.ldapAuthenticationHandler.setAttributesToReturn(list);   
    }
}
    
