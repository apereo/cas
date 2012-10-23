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
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.InitializingBean;

public class LdapPasswordPolicyAwareAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {

    private enum LdapUserAccountControlFlags {
        UAC_FLAG_ACCOUNT_DISABLED(2),
        UAC_FLAG_LOCKOUT(16),
        UAC_FLAG_PASSWD_NOTREQD(32),
        UAC_FLAG_DONT_EXPIRE_PASSWD(65536),
        UAC_FLAG_PASSWORD_EXPIRED(8388608);
        
        private int value;
        
        LdapUserAccountControlFlags(final int id) { 
            this.value = id; 
        }
        
        public final int getValue() { 
            return this.value; 
        }
    }
    
    /** The custom attribute that indicates the account is disabled **/
    private String accountDisabledAttributeName = null;

    /** The custom attribute that indicates the account is locked **/
    private String accountLockedAttributeName = null;

    /** The custom attribute that indicates the account password must change **/
    private String accountPasswordMustChangeAttributeName = null;

    /** The attribute that indicates the user account status **/
    private String userAccountControlAttributeName = "userAccountControl";
    
    /** Disregard the warning period and warn all users of password expiration */
    private boolean alwaysDisplayPasswordExpirationWarning = false;
    
    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String ignorePasswordExpirationWarningAttributeName = null;

    private int defaultValidPasswordNumberOfDays = 180;
    
    private int defaultPasswordWarningNumberOfDays = 30;
    
    /** The value that will cause password warning to be bypassed  */
    private List<String> ignorePasswordExpirationWarningFlags;

    private String passwordPolicyUrl;
    
    @NotNull
    private AbstractLdapUsernamePasswordAuthenticationHandler ldapAuthenticationHandler = null;

    @NotNull
    private LdapDateConverter ldapDateConverter = null;
    
    /** 
     * List of error definitions and their types, based on which the user will be directed to a given view in the flow
     * @see LdapErrorDefinition 
     **/
    private List<LdapErrorDefinition> ldapErrorDefinitions = null;

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

    public void setAccountPasswordMustChange(final String accountPasswordMustChange) {
        this.accountPasswordMustChangeAttributeName = accountPasswordMustChange;
    }

    public void setLdapDateConverter(final LdapDateConverter ldapDateConverter) {
        this.ldapDateConverter = ldapDateConverter;
    }
    
    public void setAlwaysDisplayPasswordExpirationWarning(final boolean warnAll) {
        this.alwaysDisplayPasswordExpirationWarning = warnAll;
    }

    public void setIgnorePasswordExpirationWarningAttributeName(final String noWarningAttributeName) {
        this.ignorePasswordExpirationWarningAttributeName = noWarningAttributeName;
    }

    public void setIgnorePasswordExpirationWarningFlags(final List<String> ignorePasswordWarningFlags) {
        this.ignorePasswordExpirationWarningFlags = ignorePasswordWarningFlags;
    }

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
     * 
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
        
    /**
     * Calculates the number of days left to the expiration date based on the {@code expireDate} parameter
     * @param expireDate
     * @param userId
     * @return number of days left to the expiration date, or {@value #PASSWORD_STATUS_PASS}
     */
    private int getDaysToExpirationDate(final LdapPasswordPolicyConfiguration config, final DateTime expireDate)
            throws LdapPasswordPolicyEnforcementException {

        log.debug("Calculating number of days left to the expiration date for user {}", config.getUserId());

        final DateTime currentTime = new DateTime(this.ldapDateConverter.getTimeZone());
        log.info("Current date is {}. Expiration date is {}" + currentTime, expireDate);

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            String msgToLog = "Authentication failed because account password has expired with " + daysToExpirationDate + " to expiration date. ";
            msgToLog += "Verify the value of the " + this.passwordExpirationDateAttributeName
                    + " attribute and make sure it's not before the current date, which is " + currentTime.toString();

            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException(msgToLog);

            log.error(msgToLog, exc);
            throw exc;
        }

        // Warning period begins from X number of ways before the expiration date
        final DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), this.ldapDateConverter.getTimeZone()).minusDays(config
                .getPasswordWarningNumberOfDays());
        log.info("Warning period begins on {}", warnPeriod.toString());

        if (this.alwaysDisplayPasswordExpirationWarning) {
            log.info("Warning all. The password for {} will expire in {} day(s)", config.getUserId(), daysToExpirationDate);
        } else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod)) {
            log.info("Password will expire in {} day(s)", daysToExpirationDate);
        } else {
            log.info("Password is not expiring. {} day(s) left to the warning", daysToExpirationDate);
            daysToExpirationDate = -1;
        }

        return daysToExpirationDate;
    }

    /**
     * Determines the expiration date to use based on the settings.
     */
    private DateTime getExpirationDateToUse(final LdapPasswordPolicyConfiguration config) {
        final DateTime dateValue = this.ldapDateConverter.convert(config.getPasswordExpirationDate());
        final DateTime expireDate = dateValue.plusDays(config.getValidPasswordNumberOfDays());
        log.debug("Retrieved date value {} for date attribute {} and added {} days. The final expiration date is {}", dateValue,
                config.getPasswordExpirationDate(), config.getValidPasswordNumberOfDays(), expireDate);

        return expireDate;
    }

    
    private void examineAccountPasswordPolicy(final UsernamePasswordCredentials credentials, final LdapPasswordPolicyConfiguration passwordPolicyConfig) throws LdapAuthenticationException {  
      if (this.isAccountPasswordSetToNeverExpire(passwordPolicyConfig)) {
          log.debug("Account password will never expire. Skipping password warning check...");
          return;
      }
  
      final DateTime expireTime = this.getExpirationDateToUse(passwordPolicyConfig);
  
      if (expireTime == null) {
          final String msg = String
                  .format("Expiration date cannot be determined for date %s", passwordPolicyConfig.getPasswordExpirationDate());
          throw new LdapAuthenticationException(msg);
      }
  
      final int days = this.getDaysToExpirationDate(passwordPolicyConfig, expireTime);
      if (days != -1) {
          LdapPasswordPolicyEnforcementException ex = new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE, 
                                                          String.format("Account password will expire in %d days", days), 
                                                          "passwordExpirationWarning");
          ex.setNumberOfDaysToPasswordExpirationDate(days); 
          throw ex;
      }        
    }

    @Override
    protected boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {
        try {
            final boolean authenticated = this.ldapAuthenticationHandler.authenticate(credentials);
            if (authenticated) {
                
                final SearchResult authenticatedDn = this.ldapAuthenticationHandler.getAuthenticatedDistinguishedNameSearchResult();
                if (authenticatedDn == null) {
                    log.warn("Authentication handler {} has indicated success, but the authentication DN cannot be found. Ignoring password policy checks...",
                            this.ldapAuthenticationHandler.getName());
                    return authenticated;
                }

                final LdapPasswordPolicyConfiguration passwordPolicyConfig = this.getPasswordPolicyConfiguration(authenticatedDn);
                
                if (passwordPolicyConfig == null) {
                    log.warn("Password policy configuration could not be connstructed. Skipping all password policy checks...");
                    return authenticated;
                }
                
                examineAccountStatus(credentials, passwordPolicyConfig);
                examineAccountPasswordPolicy(credentials, passwordPolicyConfig);
            }
            return authenticated;
        } catch (final AuthenticationException e) {
            throw this.handleLdapError(e);
        }
    }

    private void examineAccountStatus(final UsernamePasswordCredentials credentials, final LdapPasswordPolicyConfiguration passwordPolicyConfig) throws AuthenticationException {
        
        if (NumberUtils.isNumber(passwordPolicyConfig.getUserAccountControl())) {
           final int uacValue = Integer.parseInt(passwordPolicyConfig.getUserAccountControl());
           if ((uacValue & LdapUserAccountControlFlags.UAC_FLAG_ACCOUNT_DISABLED.getValue()) == 
                   LdapUserAccountControlFlags.UAC_FLAG_ACCOUNT_DISABLED.getValue()) {
               throw new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, 
                                                     String.format("Account %s is disabled", passwordPolicyConfig.getUserId()),
                                                     new AccountDisabledLdapErrorDefinition().getType());
           } 
           
           if ((uacValue & LdapUserAccountControlFlags.UAC_FLAG_LOCKOUT.getValue()) == 
                   LdapUserAccountControlFlags.UAC_FLAG_LOCKOUT.getValue()) {
               throw new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, 
                                                     String.format("Account %s is locked", passwordPolicyConfig.getUserId()),
                                                     new AccountLockedLdapErrorDefinition().getType());
           } 
        }

        if (passwordPolicyConfig.isAccountDisabled()) {
            throw new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, 
                                                  String.format("Account %s is disabled", passwordPolicyConfig.getUserId()),
                                                  new AccountDisabledLdapErrorDefinition().getType());
        }
        
        if (passwordPolicyConfig.isAccountLocked()) {
            throw new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, 
                                                  String.format("Account %s is locked", passwordPolicyConfig.getUserId()),
                                                  new AccountLockedLdapErrorDefinition().getType());
        }
        
        if (passwordPolicyConfig.isAccountPasswordMustChange()) {
            throw new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, 
                                                  String.format("Account %s must change it password", passwordPolicyConfig.getUserId()),
                                                  new AccountMustChangePasswordLdapErrorDefinition().getType());
        }
    }

    private String getPasswordPolicyAttributeValue(final Attributes attrs, final String attrName) throws Exception {
        if (attrName != null) {
            if (attrs.get(attrName) != null) {
                final String value = (String) attrs.get(attrName).get();
                return value;
            }
        }
        return null;
    }
    
    protected LdapPasswordPolicyConfiguration getPasswordPolicyConfiguration(final SearchResult authenticatedDn) throws LdapAuthenticationException {
        LdapPasswordPolicyConfiguration configuration = null;
        try {

            final Attributes attrs = authenticatedDn.getAttributes();
            final String expirationDate = getPasswordPolicyAttributeValue(attrs, this.passwordExpirationDateAttributeName);

            if (expirationDate == null) {
                log.warn("Password expiration date does not contain a value for attribute {}", this.passwordExpirationDateAttributeName);
                return null;
            }
             
            configuration = new LdapPasswordPolicyConfiguration(authenticatedDn.getNameInNamespace());
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
            throw new LdapAuthenticationException(e.getMessage());
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
            return BadCredentialsAuthenticationException.ERROR;
        }

        log.debug("Handling error {}", e.getMessage());
        for (final LdapErrorDefinition ldapErrorDef : this.ldapErrorDefinitions) {
            if (ldapErrorDef.matches(e.getMessage())) {
                log.debug("Found ldap error definition {}. Throwing error for {}", ldapErrorDef, e.getMessage());
                return new LdapAuthenticationException(BadCredentialsAuthenticationException.CODE, e.getMessage(), ldapErrorDef.getType());
            }
        }

        log.debug("No error definition could be matched against the error. Throwing default error for {}", e.getMessage());
        return BadCredentialsAuthenticationException.ERROR;
    }

    /**
     * Determines if the password value is set to never expire.
     *
     * @return boolean that indicates whether  or not password warning should proceed.
     */
    protected boolean isAccountPasswordSetToNeverExpire(final LdapPasswordPolicyConfiguration config) {
        final String ignoreCheckValue = config.getIgnorePasswordExpirationWarning();
        boolean ignoreChecks = ignoreCheckValue == null;
        
        if (!ignoreChecks && this.ignorePasswordExpirationWarningFlags != null && this.ignorePasswordExpirationWarningFlags.size() > 0) {
            ignoreChecks = this.ignorePasswordExpirationWarningFlags.contains(ignoreCheckValue);
        }
    
        if (!ignoreChecks && NumberUtils.isNumber(config.getUserAccountControl())) {
            final int uacValue = Integer.parseInt(config.getUserAccountControl());
            ignoreChecks = ((uacValue & LdapUserAccountControlFlags.UAC_FLAG_DONT_EXPIRE_PASSWD.getValue()) == 
                           LdapUserAccountControlFlags.UAC_FLAG_DONT_EXPIRE_PASSWD.getValue());
        }
        return ignoreChecks;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final List<String> list = new ArrayList<String>(this.ldapAuthenticationHandler.getAttributesToReturn());
        
        if (StringUtils.isBlank(this.userAccountControlAttributeName)) {
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
