/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.management.timer.Timer;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.jasig.cas.authentication.AbstractPasswordPolicyEnforcer;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.util.LdapUtils;
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

        private String DateResult;
        private String noWarnAttributeResult;
        private String validDaysResult;
        private String warnDaysResult;

        public String getDateResult() {
            return DateResult;
        }

        public String getNoWarnAttributeResult() {
            return noWarnAttributeResult;
        }

        public String getValidDaysResult() {
            return validDaysResult;
        }

        public String getWarnDaysResult() {
            return warnDaysResult;
        }

        public void setDateResult(final String date) {
            DateResult = date;
        }

        public void setNoWarnAttributeResult(final String noWarnAttributeResult) {
            this.noWarnAttributeResult = noWarnAttributeResult;
        }

        public void setValidDaysResult(final String valid) {
            validDaysResult = valid;
        }

        public void setWarnDaysResult(final String warn) {
            warnDaysResult = warn;
        }
    }

    private static final String CHECK_TYPE_PWDCHANGE               = "change";

    private static final String CHECK_TYPE_PWDEXPIRE               = "expire";

    /** The default maximum number of results to return. */
    private static final int    DEFAULT_MAX_NUMBER_OF_RESULTS      = 10;

    /** The default timeout. */
    private static final int    DEFAULT_TIMEOUT                    = 1000;

    /** The list of valid scope values. */
    private static final int[]  VALID_SCOPE_VALUES                 = new int[] { SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE                          };

    private String[]            attributeIds;

    /** The filter path to the lookup value of the user. */
    private String              filter;

    /** Whether the LdapTemplate should ignore partial results. */
    private boolean             ignorePartialResultException       = false;

    /** Time Segments for AD Password age **/
    private final long                INTERVALS_PER_MILLISECOND          = 10000;

    /** LdapTemplate to execute ldap queries. */
    private LdapTemplate        ldapTemplate;

    /** The maximum number of results to return. */
    private int                 maxNumberResults                   = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** Number of milliseconds between 1/1/1601 and 1/1/1970 **/
    private final long                MILLISECONDS_BETWEEN_1601_AND_1970 = Timer.ONE_DAY * 134775;

    /** The attribute that contains the data that will determine if password warning is skipped  */
    private String              noWarnAttribute;

    /** The value that will cause password warning to be bypassed  */
    private List<String>        noWarnValues;

    /** The scope. */
    private int                 scope                              = SearchControls.SUBTREE_SCOPE;

    /** The search base to find the user under. */
    private String              searchBase;

    /** The amount of time to wait. */
    private int                 timeout                            = DEFAULT_TIMEOUT;

    /** default number of days a password is valid */
    private int                 validDays;

    /** default number of days that a warning message will be displayed */
    private int                 warningDays;

    /** The attribute that contains the date the password will expire or last password change */
    protected String            dateAttribute;

    /** The format of the date in DateAttribute */
    protected String            dateFormat;

    /** The attribute that contains the number of days the user's password is valid */
    protected String            validDaysAttribute;

    /** Disregard WarnPeriod and warn all users of password expiration */
    protected Boolean           warnAll;

    /** Calculate password warning from last password change time or expiration date */
    protected String            warningCheckType;

    /** The attribute that contains the user's warning days */
    protected String            warningDaysAttribute;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(filter, "filter cannot be null");
        Assert.notNull(searchBase, "searchBase cannot be null");
        Assert.notNull(warningCheckType, "warningCheckType cannot be null");
        Assert.notNull(warnAll, "warnAll cannot be null");
        Assert.notNull(dateAttribute, "dateAttribute cannot be null");
        Assert.notNull(dateFormat, "dateFormat cannot be null");
        Assert.notNull(warningDays, "warningDays cannot be null");
        Assert.isTrue(filter.contains("%u"), "filter must contain %u");

        ldapTemplate.setIgnorePartialResultException(ignorePartialResultException);

        for (final int element : VALID_SCOPE_VALUES)
            if (scope == element)
                return;
        throw new IllegalStateException("You must set a valid scope.");
    }

    public int getNumberOfDaysToPasswordExpirationDate(final String userId) throws LdapPasswordPolicyEnforcementException {
        String msgToLog = null;

        final List<String> attributeList = new ArrayList<String>();

        if (dateAttribute == null) {
            msgToLog = "Date attribute is not configured.";
            final LdapPasswordPolicyEnforcementException exc = new LdapPasswordPolicyEnforcementException("error.authentication.credentials.bad", msgToLog);

            if (logger.isErrorEnabled())
                logger.error(msgToLog, exc);

            throw exc;
        }

        attributeList.add(dateAttribute);

        if (warningDaysAttribute != null)
            attributeList.add(warningDaysAttribute);

        if (validDaysAttribute != null)
            attributeList.add(validDaysAttribute);

        if (noWarnAttribute != null)
            attributeList.add(noWarnAttribute);

        attributeIds = new String[attributeList.size()];
        attributeList.toArray(attributeIds);

        final LdapResult ldapResult = getResultsFromLDAP(userId);

        if (ldapResult == null) {
            msgToLog = "No entry was found for user " + userId;

            if (logger.isErrorEnabled())
                logger.error(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(BadCredentialsAuthenticationException.CODE, msgToLog);
        }

        final String warnDaysResult = ldapResult.getWarnDaysResult();
        final String validDaysResult = ldapResult.getValidDaysResult();
        final String warnAttribute = ldapResult.getNoWarnAttributeResult();

        logger.debug("Warning flag is set to: " + warnAttribute);

        if (noWarnAttribute != null)
            if (noWarnValues.contains(warnAttribute)) {

                if (logger.isInfoEnabled())
                    logger.info("No warning flag is set. Skipping password warning check");

                return -1;
            }

        if (warnDaysResult == null)
            if (logger.isDebugEnabled())
                logger.debug("No warning days value is found for " + userId + ". Using system default of " + warningDays);
        else
            warningDays = Integer.parseInt(warnDaysResult);

        final long currentTime = new Date().getTime();
        long expireTime = currentTime;

        if (warningCheckType.equalsIgnoreCase(CHECK_TYPE_PWDCHANGE)) {
            final String changeDateResult = ldapResult.getDateResult();

            if (logger.isDebugEnabled()) {
              logger.debug("Calculating warning period from last change date");
              logger.debug("warnDays:" + warnDaysResult + ", changeDate:" + changeDateResult + ", validDays:" + validDaysResult);
            }

            if (changeDateResult == null) {
                msgToLog = "No password change date for " + userId;

                if (logger.isWarnEnabled())
                    logger.warn(msgToLog);

                throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_CHANGE, msgToLog);
            }

            GregorianCalendar changeDate = new GregorianCalendar();
            if (isUsingActiveDirectory())
                changeDate = convertDateAD(changeDateResult);
            else {
                changeDate = convertDate(changeDateResult, dateFormat);
                if (changeDate == null) {
                    msgToLog = "Last password change date is invalid.";

                    if (logger.isWarnEnabled())
                        logger.warn(msgToLog);

                    throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_CHANGE, msgToLog);
                }
            }

            if (validDaysResult == null)
                if (logger.isDebugEnabled())
                    logger.debug("No maximum password age found for " + userId + ". Using system default of " + validDays + " days");
            else
                validDays = Integer.parseInt(validDaysResult);

            //Password expiration is the time the password was last changed plus the maximum password age
            final long validPeriod = validDays * Timer.ONE_DAY;
            final long changeTime = changeDate.getTime().getTime();
            expireTime = changeTime + validPeriod;

            //The expiration date is stored in LDAP
        } else if (warningCheckType.equalsIgnoreCase(CHECK_TYPE_PWDEXPIRE)) {

            final String expireDateResult = ldapResult.getDateResult();

            if (logger.isDebugEnabled())  {
              logger.debug("Calculating warning period from expiration date...");
              logger.debug("warnDays:" + warnDaysResult + ", expireDate:" + expireDateResult);
            }

            if (expireDateResult == null) {
                msgToLog = "Expiration date value for " + userId + " is null.";

                if (logger.isWarnEnabled())
                    logger.warn(msgToLog);

                throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_CHANGE, msgToLog);
            }

            final GregorianCalendar expireDate = convertDate(expireDateResult, dateFormat);
            if (expireDate == null) {
                msgToLog = "The calculated expiration date to calendar time is null.";

                if (logger.isWarnEnabled())
                    logger.warn(msgToLog);

                throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_CHANGE, msgToLog);
            }

            expireTime = expireDate.getTime().getTime();

        } else {
            msgToLog = "Invalid value for warningCheckType: " + warningCheckType;

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_CHANGE, msgToLog);
        }

        final float dateDiff = expireTime - currentTime;
        final float warnPeriod = warningDays * Timer.ONE_DAY;

        if (logger.isDebugEnabled())
          logger.debug("Current time:" + currentTime / 1000 + ", expiration time:" + expireTime / 1000
                  + ", difference (seconds):" + dateDiff + ", warn period (seconds):" + warnPeriod);

        //The LDAP server should have thrown an error because the password has already expired
        if (dateDiff < 0) {
            msgToLog = "Expiration date has passed for " + userId + " but authentication succeeded.";

            if (logger.isWarnEnabled())
                logger.warn(msgToLog);

            throw new LdapPasswordPolicyEnforcementException(LdapPasswordPolicyEnforcementException.CODE_PASSWORD_EXPIRED, msgToLog);
        }

        final int daysToExpirationDate = Math.round(dateDiff / Timer.ONE_DAY);
        if (warnAll) {

            if (logger.isInfoEnabled())
                logger.info("The password for " + userId + " will expire in " + daysToExpirationDate + " days");

            return daysToExpirationDate;
        }

        final int comp = Float.compare(dateDiff, warnPeriod);
        if (comp > 0) {

            if (logger.isDebugEnabled())
                logger.debug("Skip warning. The password for " + userId + " will expire in " + daysToExpirationDate + " days");

            return -1;
        } else {

            if (logger.isInfoEnabled())
                logger.info("Show warning. The password for " + userId + " will expire in " + daysToExpirationDate + " days");

            return daysToExpirationDate;
        }

    }

    /**
     * Method to set the data source and generate a LDAPTemplate.
     *
     * @param dataSource the data source to use.
     */
    public void setContextSource(final ContextSource contextSource) {
        ldapTemplate = new LdapTemplate(contextSource);
    }

    /**
     * @param DateAttribute The DateAttribute to set.
     */
    public void setDateAttribute(final String DateAttribute) {
        dateAttribute = DateAttribute;

        if (logger.isInfoEnabled())
            logger.info("Date attribute: " + DateAttribute );
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
            logger.info("Search filter: " + filter );
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
            logger.info("Attribute to flag warning bypass: " + noWarnAttribute );
    }

    /**
     * @param noWarnAttribute The noWarnAttribute to set.
     */
    public void setNoWarnValues(final List<String> noWarnValues) {
        this.noWarnValues = noWarnValues;

        if (logger.isInfoEnabled())
            logger.info("Value to flag warning bypass: " + noWarnValues.toString() );
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
        validDaysAttribute = ValidDaysAttribute;

        if (logger.isInfoEnabled())
            logger.info("Valid Days Attribute: " + ValidDaysAttribute );
    }

    /**
     * @param warnAll Disregard warningPeriod and warn all users of password expiration.
     */
    public void setWarnAll(final Boolean warnAll) {
        this.warnAll = warnAll;

        if (logger.isInfoEnabled())
            logger.info("warnAll: " + warnAll );
    }

    /**
     * @param warningCheckType The warningCheckType to set.
     */
    public void setWarningCheckType(final String WarningCheckType) {
        warningCheckType = WarningCheckType;

        if (logger.isInfoEnabled())
            logger.info("warningCheckType: " + WarningCheckType);
    }

    /**
     * @param warningDays Number of days before expiration that a warning
     * message is displayed to set. Used as a default if warningDaysAttribute is
     * not set or is not found in the LDAP results. This parameter is required.
     */
    public void setWarningDays(final int warningDays) {
        this.warningDays = warningDays;

        if (logger.isInfoEnabled())
            logger.info("Default warning days: " + warningDays);
    }

    /**
     * @param WarningDaysAttribute The WarningDaysAttribute to set.
     */
    public void setWarningDaysAttribute(final String WarningDaysAttribute) {
        warningDaysAttribute = WarningDaysAttribute;

        if (logger.isInfoEnabled())
            logger.info("Warning Days Attribute: " + WarningDaysAttribute);
    }

    private GregorianCalendar convertDate(final String ldapResult, final String dateFormat) {
        final DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        format.setLenient(false);
        Date date = null;
        try {
            date = format.parse(ldapResult);
        } catch (final ParseException e) {

            if (logger.isWarnEnabled())
                logger.warn("The attribute " + dateAttribute + " with value " + ldapResult + " should contain a date in the format " + dateFormat);

            return null;
        }
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return calendar;
    }

    private GregorianCalendar convertDateAD(final String pwdLastSet) {
        final long l = Long.parseLong(pwdLastSet.trim());
        long t = l / INTERVALS_PER_MILLISECOND;

        if (logger.isDebugEnabled())
            logger.debug("pwdLastSet= " + t + " millisec since 1601");

        t -= MILLISECONDS_BETWEEN_1601_AND_1970;

        if (logger.isDebugEnabled())
            logger.debug("pwdLastSet= " + t + " millisec since 1970");

        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(t);

        if (logger.isDebugEnabled())
            logger.debug("pwdLastSet= " + calendar.getTime().toString());

        return calendar;
    }

    private LdapResult getResultsFromLDAP(final String userID) {
        final String searchFilter = LdapUtils.getFilterWithValues(filter, userID);

        if (logger.isDebugEnabled())
            logger.debug("Starting search with searchFilter: " + searchFilter );

        String attributeListLog = attributeIds[0];

        for (int i = 1; i < attributeIds.length; i++)
            attributeListLog = attributeListLog.concat(":" + attributeIds[i]);

        if (logger.isDebugEnabled())
            logger.debug("Returning attributes " + attributeListLog);

        try {

            final AttributesMapper mapper = new AttributesMapper() {

                public Object mapFromAttributes(final Attributes attrs) throws NamingException {
                    final LdapResult result = new LdapResult();

                    if (dateAttribute != null)
                        if (attrs.get(dateAttribute) != null) {
                            final String date = (String) attrs.get(dateAttribute).get();
                            result.setDateResult(date);
                        }

                    if (warningDaysAttribute != null)
                        if (attrs.get(warningDaysAttribute) != null) {
                            final String warn = (String) attrs.get(warningDaysAttribute).get();
                            result.setWarnDaysResult(warn);
                        }

                    if (noWarnAttribute != null)
                        if (attrs.get(noWarnAttribute) != null) {
                            final String Attrib = (String) attrs.get(noWarnAttribute).get();
                            result.setNoWarnAttributeResult(Attrib);
                        }

                    if (warningCheckType.equalsIgnoreCase(CHECK_TYPE_PWDCHANGE))
                        if (attrs.get(validDaysAttribute) != null) {
                            final String valid = (String) attrs.get(validDaysAttribute).get();
                            result.setValidDaysResult(valid);
                        }

                    return result;
                }
            };

            final List<?> LdapResultList = ldapTemplate.search(searchBase, searchFilter, getSearchControls(attributeIds), mapper);

            if (LdapResultList.size() > 0)
                return (LdapResult) LdapResultList.get(0);
        } catch (final Exception e) {
            logger.error("Error searching the directory", e);

        }
        return null;

    }

    private SearchControls getSearchControls(final String[] attributeIds) {
        final SearchControls constraints = new SearchControls();

        constraints.setSearchScope(scope);
        constraints.setReturningAttributes(this.attributeIds);
        constraints.setTimeLimit(timeout);
        constraints.setCountLimit(maxNumberResults);

        return constraints;
    }

    private boolean isUsingActiveDirectory() {
        return dateFormat.equalsIgnoreCase("ActiveDirectory") || dateFormat.equalsIgnoreCase("AD");
    }
}
