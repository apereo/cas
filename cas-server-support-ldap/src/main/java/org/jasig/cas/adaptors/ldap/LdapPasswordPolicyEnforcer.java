/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.jasig.cas.authentication.AbstractPasswordPolicyEnforcer;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
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

    private static final class LdapResult {

        private String DateResult            = null;
        private String noWarnAttributeResult = null;
        private String userId                = null;
        private String validDaysResult       = null;
        private String warnDaysResult        = null;

        public LdapResult(final String userId) {
            this.userId = userId;
        }

        public String getDateResult() {
            return this.DateResult;
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
            this.DateResult = date;
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


    private static final DateTimeZone DEFAULT_TIME_ZONE     = DateTimeZone.UTC;

    private static final String CHECK_TYPE_PWDCHANGE               = "change";

    private static final String CHECK_TYPE_PWDEXPIRE               = "expire";

    /** The default maximum number of results to return. */
    private static final int    DEFAULT_MAX_NUMBER_OF_RESULTS      = 10;

    /** The default timeout. */
    private static final int    DEFAULT_TIMEOUT                    = 1000;

    private static final long   YEARS_FROM_1601_1970               = 1970 - 1601;

    /**
     * Consider leap years, divide by 4.
     * Consider non-leap centuries, (1700,1800,1900). 2000 is a leap century
     */
    private static final long TOTAL_SECONDS_FROM_1601_1970 = (YEARS_FROM_1601_1970 * 365 + YEARS_FROM_1601_1970 / 4 - 3) * 24 * 60 * 60;

    /** The list of valid scope values. */
    private static final int[]  VALID_SCOPE_VALUES            = new int[] { SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE                     };

    /** The filter path to the lookup value of the user. */
    private String              filter;

    /** Whether the LdapTemplate should ignore partial results. */
    private boolean             ignorePartialResultException       = false;

    /** LdapTemplate to execute ldap queries. */
    private LdapTemplate        ldapTemplate;

    /** The maximum number of results to return. */
    private int                 maxNumberResults                   = LdapPasswordPolicyEnforcer.DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String              noWarnAttribute;

    /** The value that will cause password warning to be bypassed  */
    private List<String>        noWarnValues;

    /** The scope. */
    private int                 scope                              = SearchControls.SUBTREE_SCOPE;

    /** The search base to find the user under. */
    private String              searchBase;

    /** The amount of time to wait. */
    private int                 timeout                            = LdapPasswordPolicyEnforcer.DEFAULT_TIMEOUT;

    /** default number of days a password is valid */
    private int                 validDays                          = 180;

    /** default number of days that a warning message will be displayed */
    private int                 warningDays                        = 30;

    /** The attribute that contains the date the password will expire or last password change */
    protected String            dateAttribute;

    /** The format of the date in DateAttribute */
    protected String            dateFormat;

    /** The attribute that contains the number of days the user's password is valid */
    protected String            validDaysAttribute;

    /** Disregard WarnPeriod and warn all users of password expiration */
    protected Boolean           warnAll                            = Boolean.FALSE;

    /** Calculate password warning from last password change time or expiration date */
    protected String            warningCheckType                   = CHECK_TYPE_PWDCHANGE;

    /** The attribute that contains the user's warning days */
    protected String            warningDaysAttribute;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(this.filter, "filter cannot be null");
        Assert.notNull(this.searchBase, "searchBase cannot be null");
        Assert.notNull(this.warningCheckType, "warningCheckType cannot be null");
        Assert.notNull(this.warnAll, "warnAll cannot be null");
        Assert.notNull(this.dateAttribute, "dateAttribute cannot be null");
        Assert.notNull(this.dateFormat, "dateFormat cannot be null");
        Assert.notNull(this.warningDays, "warningDays cannot be null");
        Assert.isTrue(this.filter.contains("%u"), "filter must contain %u");

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);

        for (final int element : VALID_SCOPE_VALUES)
            if (this.scope == element)
                return;
        throw new IllegalStateException("You must set a valid scope. Valid scope values are: " + Arrays.toString(VALID_SCOPE_VALUES));
    }

    public long getDaysToExpirationDate(final String userId, final DateTime expireDate) throws LdapPasswordPolicyEnforcementException {

        this.warningDays = 175;

        if (logger.isDebugEnabled())
            logger.debug("Calculating number of days left to the expiration date");

        final DateTime currentTime = new DateTime(DEFAULT_TIME_ZONE);

        logInfo("Current date is " + currentTime.toString());
        logInfo("Expiration date is " + expireDate.toString());

        if (expireDate.equals(currentTime) || expireDate.isBefore(currentTime)) {
            final String msgToLog = "Authenticated failed because account password has expired.";
            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException(msgToLog);

            if (logger.isErrorEnabled())
                logger.error(msgToLog, exc);

            throw exc;
        }

        /*
         * Warning period begins from X number of ways before the expiration date
         */
        DateTime warnPeriod = new DateTime(DateTime.parse(expireDate.toString()), DEFAULT_TIME_ZONE);

        warnPeriod = warnPeriod.minusDays(this.warningDays);
        logInfo("Warning period begins on " + warnPeriod.toString());

        final Days d = Days.daysBetween(currentTime, expireDate);
        int daysToExpirationDate = d.getDays();

        if (this.warnAll)
            logInfo("Warning all. The password for " + userId + " will expire in " + daysToExpirationDate + " days.");
        else if (currentTime.equals(warnPeriod) || currentTime.isAfter(warnPeriod))
            logInfo("Password will expire in " + daysToExpirationDate + " days.");
        else {
            logInfo("Password is not expiring. " + daysToExpirationDate + " days left to the warning");
            daysToExpirationDate = -1;
        }

        return daysToExpirationDate;
    }

    public long getNumberOfDaysToPasswordExpirationDate(final String userId) throws LdapPasswordPolicyEnforcementException {
        String msgToLog = null;

        if (this.dateAttribute == null) {
            msgToLog = "Date attribute is not configured.";
            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException(BadCredentialsAuthenticationException.CODE,
                    msgToLog);

            if (logger.isErrorEnabled())
                logger.error(msgToLog, exc);

            throw exc;
        }

        final LdapResult ldapResult = getResultsFromLDAP(userId);

        if (ldapResult == null) {
            msgToLog = "No entry was found for user " + userId;

            if (logger.isErrorEnabled())
                logger.error(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(BadCredentialsAuthenticationException.CODE, msgToLog);
        }

        logger.debug("Warning flag is set to: " + ldapResult.getNoWarnAttributeResult());

        if (this.noWarnAttribute != null)
            if (this.noWarnValues.contains(ldapResult.getNoWarnAttributeResult())) {
                if (logger.isInfoEnabled())
                    logger.info("No warning flag is set. Skipping password warning check");

                return -1;
            }

        if (ldapResult.getWarnDaysResult() == null)
            if (logger.isDebugEnabled())
                logger.debug("No warning days value is found for " + userId + ". Using system default of " + this.warningDays);
            else
                this.warningDays = Integer.parseInt(ldapResult.getWarnDaysResult());

        if (ldapResult.getValidDaysResult() == null)
            if (logger.isDebugEnabled())
                logger.debug("No maximum password valid days found for " + ldapResult.getUserId() + ". Using system default of " + this.validDays
                        + " days");
            else
                this.validDays = Integer.parseInt(ldapResult.getValidDaysResult());

        DateTime expireTime = null;

        if (this.warningCheckType.equalsIgnoreCase(LdapPasswordPolicyEnforcer.CHECK_TYPE_PWDCHANGE))
            expireTime = getPasswordExpirationTimeForChangeCheckType(ldapResult);
        else if (this.warningCheckType.equalsIgnoreCase(LdapPasswordPolicyEnforcer.CHECK_TYPE_PWDEXPIRE))
            expireTime = getPasswordExpirationTimeForExpireCheckType(ldapResult);
        else {
            msgToLog = "Invalid value for warningCheckType: " + this.warningCheckType;

            if (logger.isErrorEnabled())
                logger.error(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(msgToLog);
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
    public void setDateAttribute(final String DateAttribute) {
        this.dateAttribute = DateAttribute;

        if (logger.isInfoEnabled())
            logger.info("Date attribute: " + DateAttribute);
    }

    /**
     * @param dateFormat String to pass to SimpleDateFormat() that describes the
     * date in the ExpireDateAttribute. This parameter is required.
     */
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;

        if (logger.isInfoEnabled())
            logger.info("Date format: " + dateFormat);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;

        if (logger.isInfoEnabled())
            logger.info("Search filter: " + filter);
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

        if (logger.isInfoEnabled())
            logger.info("Attribute to flag warning bypass: " + noWarnAttribute);
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public void setNoWarnValues(final List<String> noWarnValues) {
        this.noWarnValues = noWarnValues;

        if (logger.isInfoEnabled())
            logger.info("Value to flag warning bypass: " + noWarnValues.toString());
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

        if (logger.isInfoEnabled())
            logger.info("Search Base: " + searchBase);
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * @param validDays Number of days that a password is valid for.
     * Used as a default if DateAttribute is not set or is not found in the LDAP results
     */
    public void setValidDays(final int validDays) {
        this.validDays = validDays;

        if (logger.isInfoEnabled())
            logger.info("Password max age in days: " + validDays);
    }

    /**
     * @param ValidDaysAttribute The ValidDaysAttribute to set.
     */
    public void setValidDaysAttribute(final String ValidDaysAttribute) {
        this.validDaysAttribute = ValidDaysAttribute;

        if (logger.isInfoEnabled())
            logger.info("Valid Days Attribute: " + ValidDaysAttribute);
    }

    /**
     * @param warnAll Disregard warningPeriod and warn all users of password expiration.
     */
    public void setWarnAll(final Boolean warnAll) {
        this.warnAll = warnAll;

        if (logger.isInfoEnabled())
            logger.info("warnAll: " + warnAll);
    }

    /**
     * @param warningCheckType The warningCheckType to set.
     */
    public void setWarningCheckType(final String type) {
        this.warningCheckType = type;

        if (logger.isInfoEnabled())
            logger.info("warningCheckType: " + type);
    }

    /**
     * @param warningDays Number of days before expiration that a warning
     * message is displayed to set. Used as a default if warningDaysAttribute is
     * not set or is not found in the LDAP results. This parameter is required.
     */
    public void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;

        if (logger.isInfoEnabled())
            logger.info("Default warningDays: " + warningDays);
    }

    /**
     * @param WarningDaysAttribute The WarningDaysAttribute to set.
     */
    public void setWarningDaysAttribute(final String warnDays) {
        this.warningDaysAttribute = warnDays;

        if (logger.isInfoEnabled())
            logger.info("Warning days Attribute: " + warnDays);
    }

    private DateTime convertDate(final String ldapResult) {
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(this.dateFormat);
        final DateTime date = new DateTime(DateTime.parse(ldapResult, fmt), DEFAULT_TIME_ZONE);

        return date;
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

        if (logger.isInfoEnabled())
            logger.info("Recalculated ActiveDirectory pwdLastSet to " + dt.toString());

        return dt;
    }

    private DateTime getPasswordExpirationTimeForChangeCheckType(final LdapResult ldapResult) throws LdapPasswordPolicyEnforcementException {
        String msgToLog;

        final String changeDateResult = ldapResult.getDateResult();

        if (changeDateResult == null) {
            msgToLog = "No password change date for " + ldapResult.getUserId();

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(msgToLog);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Calculating warning period from last change date");
            logger.debug("warnDays:" + this.warningDays + ", changeDate:" + changeDateResult + ", validDays:" + this.validDays);
        }

        DateTime changeDate = null;
        if (isUsingActiveDirectory())
            changeDate = convertDateToActiveDirectoryFormat(changeDateResult);
        else
            changeDate = convertDate(changeDateResult);

        if (changeDate == null) {
            msgToLog = "Last password change date is invalid.";

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(msgToLog);
        }

        //Password expiration is the time the password was last changed plus the maximum password age
        final DateTime expireDate = changeDate.plusDays(this.validDays);

        if (logger.isDebugEnabled()) {
            logger.debug("Calculated expiration date from the starting date " + changeDate.toString() + ", added " + this.validDays
                    + " days.");
            logger.debug("The final expiration date is " + expireDate.toString());
        }

        return expireDate;
    }

    private DateTime getPasswordExpirationTimeForExpireCheckType(final LdapResult ldapResult) throws LdapPasswordPolicyEnforcementException {

        String msgToLog;
        final String expireDateResult = ldapResult.getDateResult();

        if (logger.isDebugEnabled()) {
            logger.debug("Calculating warning period from expiration date...");
            logger.debug("warnDays:" + this.warningDays + ", expireDate:" + expireDateResult);
        }

        if (expireDateResult == null) {
            msgToLog = "Expiration date value for " + ldapResult.getUserId() + " is null.";

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(msgToLog);
        }

        final DateTime expireDate = convertDate(expireDateResult);
        if (expireDate == null) {
            msgToLog = "The calculated expiration date to calendar time is null.";

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(msgToLog);
        }

        return expireDate;
    }

    private LdapResult getResultsFromLDAP(final String userId) {

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

        if (logger.isDebugEnabled())
            logger.debug("Starting search with searchFilter: " + searchFilter);

        String attributeListLog = attributeIds[0];

        for (int i = 1; i < attributeIds.length; i++)
            attributeListLog = attributeListLog.concat(":" + attributeIds[i]);

        if (logger.isDebugEnabled())
            logger.debug("Returning attributes " + attributeListLog);

        try {

            final AttributesMapper mapper = new AttributesMapper() {

                public Object mapFromAttributes(final Attributes attrs) throws NamingException {
                    final LdapResult result = new LdapResult(userId);

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
                            final String Attrib = (String) attrs.get(LdapPasswordPolicyEnforcer.this.noWarnAttribute).get();
                            result.setNoWarnAttributeResult(Attrib);
                        }

                    if (LdapPasswordPolicyEnforcer.this.warningCheckType.equalsIgnoreCase(LdapPasswordPolicyEnforcer.CHECK_TYPE_PWDCHANGE))
                        if (attrs.get(LdapPasswordPolicyEnforcer.this.validDaysAttribute) != null) {
                            final String valid = (String) attrs.get(LdapPasswordPolicyEnforcer.this.validDaysAttribute).get();
                            result.setValidDaysResult(valid);
                        }

                    return result;
                }
            };

            final List<?> LdapResultList = this.ldapTemplate.search(this.searchBase, searchFilter, getSearchControls(attributeIds), mapper);

            if (LdapResultList.size() > 0)
                return (LdapResult) LdapResultList.get(0);
        } catch (final Exception e) {

            if (logger.isErrorEnabled())
                logger.error(e.getMessage(), e);
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

    private boolean isUsingActiveDirectory() {
        return this.dateFormat.equalsIgnoreCase("ActiveDirectory") || this.dateFormat.equalsIgnoreCase("AD");
    }

    private void logInfo(final String log) {
        if (logger.isInfoEnabled())
            logger.info(log);
    }
}
