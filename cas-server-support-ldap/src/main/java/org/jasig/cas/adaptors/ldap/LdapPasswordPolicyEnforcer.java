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
package org.jasig.cas.adaptors.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.AbstractPasswordPolicyEnforcer;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.util.LdapUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Assert;

/**
 * Class that fetches a password expiration date from an AD/LDAP database.
 * Based on AccountStatusGetter by Bart Ophelders & Johan Peeters
 *
 * @author Eric Pierce
 * @version 1.3 12/14/2009 11:47:37
 *
 */
public class LdapPasswordPolicyEnforcer extends AbstractPasswordPolicyEnforcer {

    private static final class LdapPasswordPolicyResult {

        private String dateResult            = null;
        private String noWarnAttributeResult = null;
        private String userId                = null;
        private String validDaysResult       = null;
        private String warnDaysResult        = null;

        public LdapPasswordPolicyResult(final String userId) {
            this.userId = userId;
        }

        public String getDateResult() {
            return this.dateResult;
        }

        public String getNoWarnAttributeResult() {
            return this.noWarnAttributeResult;
        }

        public String getUserId() {
            return this.userId;
        }

        public String getValidDaysResult() {
            return this.validDaysResult;
        }

        public String getWarnDaysResult() {
            return this.warnDaysResult;
        }

        public void setDateResult(final String date) {
            this.dateResult = date;
        }

        public void setNoWarnAttributeResult(final String noWarnAttributeResult) {
            this.noWarnAttributeResult = noWarnAttributeResult;
        }

        public void setValidDaysResult(final String valid) {
            this.validDaysResult = valid;
        }

        public void setWarnDaysResult(final String warn) {
            this.warnDaysResult = warn;
        }
    }

    /** Default time zone used in calculations **/
    private static final DateTimeZone DEFAULT_TIME_ZONE             = DateTimeZone.UTC;

    /** The default maximum number of results to return. */
    private static final int          DEFAULT_MAX_NUMBER_OF_RESULTS = 10;

    /** The default timeout. */
    private static final int          DEFAULT_TIMEOUT               = 1000;

    private static final long         YEARS_FROM_1601_1970          = 1970 - 1601;

    private static final int          PASSWORD_STATUS_PASS          = -1;

    /** Value set by AD that indicates an account whose password never expires **/
    private static final double       PASSWORD_STATUS_NEVER_EXPIRE  = Math.pow(2, 63) - 1;

    /**
     * Consider leap years, divide by 4.
     * Consider non-leap centuries, (1700,1800,1900). 2000 is a leap century
     */
    private static final long         TOTAL_SECONDS_FROM_1601_1970  = (YEARS_FROM_1601_1970 * 365 + YEARS_FROM_1601_1970 / 4 - 3) * 24 * 60 * 60;

    /** The list of valid scope values. */
    private static final int[]        VALID_SCOPE_VALUES            = new int[] { SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE                           };

    /** The filter path to the lookup value of the user. */
    private String                    filter;

    /** Whether the LdapTemplate should ignore partial results. */
    private boolean                   ignorePartialResultException  = false;

    /** LdapTemplate to execute ldap queries. */
    private LdapTemplate              ldapTemplate;

    /** The maximum number of results to return. */
    private int                       maxNumberResults              = LdapPasswordPolicyEnforcer.DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String                    noWarnAttribute;

    /** The value that will cause password warning to be bypassed  */
    private List<String>              noWarnValues;

    /** The scope. */
    private int                       scope                         = SearchControls.SUBTREE_SCOPE;

    /** The search base to find the user under. */
    private String                    searchBase;

    /** The amount of time to wait. */
    private int                       timeout                       = LdapPasswordPolicyEnforcer.DEFAULT_TIMEOUT;

    /** default number of days a password is valid */
    private int                       validDays                     = 180;

    /** default number of days that a warning message will be displayed */
    private int                       warningDays                   = 30;

    /** The attribute that contains the date the password will expire or last password change */
    protected String                  dateAttribute;

    /** The format of the date in DateAttribute */
    protected String                  dateFormat;

    /** The attribute that contains the number of days the user's password is valid */
    protected String                  validDaysAttribute;

    /** Disregard WarnPeriod and warn all users of password expiration */
    protected Boolean                 warnAll                       = Boolean.FALSE;

    /** The attribute that contains the user's warning days */
    protected String                  warningDaysAttribute;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(this.filter, "filter cannot be null");
        Assert.notNull(this.searchBase, "searchBase cannot be null");
        Assert.notNull(this.warnAll, "warnAll cannot be null");
        Assert.notNull(this.dateAttribute, "dateAttribute cannot be null");
        Assert.notNull(this.dateFormat, "dateFormat cannot be null");
        Assert.isTrue(this.filter.contains("%u") || this.filter.contains("%U"), "filter must contain %u");

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);

        for (final int element : VALID_SCOPE_VALUES)
            if (this.scope == element)
                return;
        throw new IllegalStateException("You must set a valid scope. Valid scope values are: " + Arrays.toString(VALID_SCOPE_VALUES));
    }

    /**
     * @return Number of days left to the expiration date, or {@value #PASSWORD_STATUS_PASS}
     */
    public long getNumberOfDaysToPasswordExpirationDate(final String userId) throws LdapPasswordPolicyEnforcementException {
        String msgToLog = null;

        final LdapPasswordPolicyResult ldapResult = getEnforcedPasswordPolicy(userId);

        if (ldapResult == null) {
            logDebug("Skipping all password policy checks...");
            return PASSWORD_STATUS_PASS;
        }

        if (!StringUtils.isEmpty(this.noWarnAttribute))
            logDebug("No warning attribute value for " + this.noWarnAttribute + " is set to: " + ldapResult.getNoWarnAttributeResult());

        if (isPasswordSetToNeverExpire(ldapResult.getNoWarnAttributeResult())) {
            logDebug("Account password will never expire. Skipping password warning check...");

            return PASSWORD_STATUS_PASS;
        }

        if (StringUtils.isEmpty(ldapResult.getWarnDaysResult()))
            logDebug("No warning days value is found for " + userId + ". Using system default of " + this.warningDays);
        else
            this.warningDays = Integer.parseInt(ldapResult.getWarnDaysResult());

        if (StringUtils.isEmpty(ldapResult.getValidDaysResult()))
            logDebug("No maximum password valid days found for " + ldapResult.getUserId() + ". Using system default of " + this.validDays + " days");
        else
            this.validDays = Integer.parseInt(ldapResult.getValidDaysResult());

        final DateTime expireTime = getExpirationDateToUse(ldapResult.getDateResult());

        if (expireTime == null) {
            msgToLog = "Expiration date cannot be determined for date " + ldapResult.getDateResult();

            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException(msgToLog);
            logError(msgToLog, exc);

            throw exc;
        }

        return getDaysToExpirationDate(userId, expireTime);
    }

    /**
     * Method to set the data source and generate a LDAPTemplate.
     *
     * @param dataSource the data source to use.
     */
    public void setContextSource(final ContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    /**
     * @param DateAttribute The DateAttribute to set.
     */
    public void setDateAttribute(final String dateAttribute) {
        this.dateAttribute = dateAttribute;
        logDebug("Date attribute: " + dateAttribute);
    }

    /**
     * @param dateFormat String to pass to SimpleDateFormat() that describes the
     * date in the ExpireDateAttribute. This parameter is required.
     */
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
        logDebug("Date format: " + dateFormat);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;

        logDebug("Search filter: " + filter);
    }

    public void setIgnorePartialResultException(final boolean ignorePartialResultException) {
        this.ignorePartialResultException = ignorePartialResultException;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public void setNoWarnAttribute(final String noWarnAttribute) {
        this.noWarnAttribute = noWarnAttribute;

        logDebug("Attribute to flag warning bypass: " + noWarnAttribute);
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public void setNoWarnValues(final List<String> noWarnValues) {
        this.noWarnValues = noWarnValues;

        logDebug("Value to flag warning bypass: " + noWarnValues.toString());
    }

    /**
     * @param filter The scope to set.
     */
    public void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
        logDebug("Search base: " + searchBase);
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
        logDebug("Timeout: " + this.timeout);
    }

    /**
     * @param validDays Number of days that a password is valid for.
     * Used as a default if DateAttribute is not set or is not found in the LDAP results
     */
    public void setValidDays(final int validDays) {
        this.validDays = validDays;
        logDebug("Password valid days: " + validDays);
    }

    /**
     * @param ValidDaysAttribute The ValidDaysAttribute to set.
     */
    public void setValidDaysAttribute(final String validDaysAttribute) {
        this.validDaysAttribute = validDaysAttribute;
        logDebug("Valid days attribute: " + validDaysAttribute);
    }

    /**
     * @param warnAll Disregard warningPeriod and warn all users of password expiration.
     */
    public void setWarnAll(final Boolean warnAll) {
        this.warnAll = warnAll;
        logDebug("warnAll: " + warnAll);
    }

    /**
     * @param warningDays Number of days before expiration that a warning
     * message is displayed to set. Used as a default if warningDaysAttribute is
     * not set or is not found in the LDAP results. This parameter is required.
     */
    public void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;
        logDebug("Default warningDays: " + warningDays);
    }

    /**
     * @param WarningDaysAttribute The WarningDaysAttribute to set.
     */
    public void setWarningDaysAttribute(final String warnDays) {
        this.warningDaysAttribute = warnDays;
        logDebug("Warning days attribute: " + warnDays);
    }

    /***
     * Converts the numbers in Active Directory date fields for pwdLastSet, accountExpires,
     * lastLogonTimestamp, lastLogon, and badPasswordTime to a common date format.
     * @param pswValue
     */
    private DateTime convertDateToActiveDirectoryFormat(final String pswValue) {
        final long l = Long.parseLong(pswValue.trim());

        final long totalSecondsSince1601 = l / 10000000;
        final long totalSecondsSince1970 = totalSecondsSince1601 - TOTAL_SECONDS_FROM_1601_1970;

        final DateTime dt = new DateTime(totalSecondsSince1970 * 1000, DEFAULT_TIME_ZONE);

        logInfo("Recalculated " + this.dateFormat + " " + this.dateAttribute + " attribute to " + dt.toString());

        return dt;
    }

    /**
     * Parses and formats the retrieved date value from Ldap
     * @param ldapResult
     * @return newly constructed date object whose value was passed
     */
    private DateTime formatDateByPattern(final String ldapResult) {
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(this.dateFormat);
        final DateTime date = new DateTime(DateTime.parse(ldapResult, fmt), DEFAULT_TIME_ZONE);
        return date;
    }

    /**
     * Determines the expiration date to use based on the settings.
     * @param ldapDateResult
     * @return Constructed the {@link #org.joda.time.DateTime DateTime}  object which indicates the expiration date
     */
    private DateTime getExpirationDateToUse(final String ldapDateResult) {
        DateTime dateValue = null;
        if (isUsingActiveDirectory())
            dateValue = convertDateToActiveDirectoryFormat(ldapDateResult);
        else
            dateValue = formatDateByPattern(ldapDateResult);
              
        DateTime expireDate = dateValue.plusDays(this.validDays);
        logDebug("Retrieved date value " + dateValue.toString() + " for date attribute " + this.dateAttribute + " and added " + this.validDays
                + " days. The final expiration date is " + expireDate.toString());

        return expireDate;
    }

    /**
     * Calculates the number of days lefty to the expiration date based on the {@code expireDate} parameter
     * @param expireDate
     * @param userId
     * @return number of days left to the expiration date, or {@value #PASSWORD_STATUS_PASS}
     */
    private long getDaysToExpirationDate(final String userId, final DateTime expireDate) throws LdapPasswordPolicyEnforcementException {

        logDebug("Calculating number of days left to the expiration date for user " + userId);

        final DateTime currentTime = new DateTime(DEFAULT_TIME_ZONE);

        logInfo("Current date is " + currentTime.toString());
        logInfo("Expiration date is " + expireDate.toString());

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            String msgToLog = "Authentication failed because account password has expired with " + daysToExpirationDate + " to expiration date. ";
            msgToLog += "Verify the value of the " + this.dateAttribute + " attribute and make sure it's not before the current date, which is "
                    + currentTime.toString();

            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException(msgToLog);

            logError(msgToLog, exc);

            throw exc;
        }

        /*
         * Warning period begins from X number of ways before the expiration date
         */
        DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), DEFAULT_TIME_ZONE);

        warnPeriod = warnPeriod.minusDays(this.warningDays);
        logInfo("Warning period begins on " + warnPeriod.toString());

        if (this.warnAll)
            logInfo("Warning all. The password for " + userId + " will expire in " + daysToExpirationDate + " days.");
        else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod))
            logInfo("Password will expire in " + daysToExpirationDate + " days.");
        else {
            logInfo("Password is not expiring. " + daysToExpirationDate + " days left to the warning");
            daysToExpirationDate = PASSWORD_STATUS_PASS;
        }

        return daysToExpirationDate;
    }

    private LdapPasswordPolicyResult getEnforcedPasswordPolicy(final String userId) {
        LdapPasswordPolicyResult ldapResult = null;

        ldapResult  = getResultsFromLdap(userId);

        if (ldapResult == null) {
            String msgToLog = "No entry was found for user " + userId + ". Verify your LPPE settings. ";
            msgToLog += "If you are not using LPPE, set the 'enabled' property to false. ";
            msgToLog += "Password policy enforcement is currently turned on but not configured.";

            if (this.logger.isWarnEnabled())
                this.logger.warn(msgToLog);
        }
        return ldapResult;
    }

    /**
     * Retrieves the password policy results from the configured ldap repository based on the attributes defined.
     * @param userId
     * @return {@code null} if the user id cannot be found, or the {@code LdapPasswordPolicyResult} instance.
     */
    private LdapPasswordPolicyResult getResultsFromLdap(final String userId) {

        String[] attributeIds;
        final List<String> attributeList = new ArrayList<String>();

        attributeList.add(this.dateAttribute);

        if (this.warningDaysAttribute != null)
            attributeList.add(this.warningDaysAttribute);

        if (this.validDaysAttribute != null)
            attributeList.add(this.validDaysAttribute);

        if (this.noWarnAttribute != null)
            attributeList.add(this.noWarnAttribute);

        attributeIds = new String[attributeList.size()];
        attributeList.toArray(attributeIds);

        final String searchFilter = LdapUtils.getFilterWithValues(this.filter, userId);

        logDebug("Starting search with searchFilter: " + searchFilter);

        String attributeListLog = attributeIds[0];

        for (int i = 1; i < attributeIds.length; i++)
            attributeListLog = attributeListLog.concat(":" + attributeIds[i]);

        logDebug("Returning attributes " + attributeListLog);

        try {
            final AttributesMapper mapper = new AttributesMapper() {
                public Object mapFromAttributes(final Attributes attrs) throws NamingException {
                    final LdapPasswordPolicyResult result = new LdapPasswordPolicyResult(userId);

                    if (LdapPasswordPolicyEnforcer.this.dateAttribute != null)
                        if (attrs.get(LdapPasswordPolicyEnforcer.this.dateAttribute) != null) {
                            final String date = (String) attrs.get(LdapPasswordPolicyEnforcer.this.dateAttribute).get();
                            result.setDateResult(date);
                        }

                    if (LdapPasswordPolicyEnforcer.this.warningDaysAttribute != null)
                        if (attrs.get(LdapPasswordPolicyEnforcer.this.warningDaysAttribute) != null) {
                            final String warn = (String) attrs.get(LdapPasswordPolicyEnforcer.this.warningDaysAttribute).get();
                            result.setWarnDaysResult(warn);
                        }

                    if (LdapPasswordPolicyEnforcer.this.noWarnAttribute != null)
                        if (attrs.get(LdapPasswordPolicyEnforcer.this.noWarnAttribute) != null) {
                            final String attrib = (String) attrs.get(LdapPasswordPolicyEnforcer.this.noWarnAttribute).get();
                            result.setNoWarnAttributeResult(attrib);
                        }

                    if (attrs.get(LdapPasswordPolicyEnforcer.this.validDaysAttribute) != null) {
                        final String valid = (String) attrs.get(LdapPasswordPolicyEnforcer.this.validDaysAttribute).get();
                        result.setValidDaysResult(valid);
                    }

                    return result;
                }
            };

            final List<?> LdapResultList = this.ldapTemplate.search(this.searchBase, searchFilter, getSearchControls(attributeIds), mapper);

            if (LdapResultList.size() > 0)
                return (LdapPasswordPolicyResult) LdapResultList.get(0);
        } catch (final Exception e) {
            logError(e.getMessage(), e);
        }
        return null;

    }

    private SearchControls getSearchControls(final String[] attributeIds) {
        final SearchControls constraints = new SearchControls();

        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(attributeIds);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    /**
     * Determines if the password value is set to never expire.
     * It will check the value against the previously defined list of {@link #noWarnValues}.
     * If that fails, checks the value against {@link #PASSWORD_STATUS_NEVER_EXPIRE}
     *
     * @return boolean that indicates whether  or not password warning should proceed.
     */
    private boolean isPasswordSetToNeverExpire(final String pswValue) {
        boolean ignoreChecks = this.noWarnValues.contains(pswValue);
        if (!ignoreChecks && StringUtils.isNumeric(pswValue)) {
            final double psw = Double.parseDouble(pswValue);
            ignoreChecks = psw == PASSWORD_STATUS_NEVER_EXPIRE;
        }
        return ignoreChecks;
    }

    /**
     * Determines whether the {@link #dateFormat} field is configured for ActiveDirectory.
     * Accepted values are {@code ActiveDirectory} or {@code AD}
     * @return boolean that says whether or not {@link #dateFormat} is defined for ActiveDirectory.
     */
    private boolean isUsingActiveDirectory() {
        return this.dateFormat.equalsIgnoreCase("ActiveDirectory") || this.dateFormat.equalsIgnoreCase("AD");
    }

    private void logDebug(final String log) {
        if (this.logger.isDebugEnabled())
            this.logger.debug(log);
    }

    private void logError(final String log, final Exception e) {
        if (this.logger.isErrorEnabled())
            this.logger.error(e.getMessage(), e);
    }

    private void logInfo(final String log) {
        if (this.logger.isInfoEnabled())
            this.logger.info(log);
    }
}
