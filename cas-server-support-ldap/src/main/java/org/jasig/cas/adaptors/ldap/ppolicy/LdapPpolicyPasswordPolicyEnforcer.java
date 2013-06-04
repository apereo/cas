/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jasig.cas.adaptors.ldap.ppolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import org.jasig.cas.authentication.AbstractPasswordPolicyEnforcer;
import org.jasig.cas.authentication.LdapPasswordPolicyEnforcementException;
import org.jasig.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControlExtractor;
import org.springframework.security.ldap.ppolicy.PasswordPolicyResponseControl;
import org.springframework.util.Assert;

/**
 *
 * @author swen
 */
public class LdapPpolicyPasswordPolicyEnforcer extends AbstractPasswordPolicyEnforcer {

    /**
     * The default maximum number of results to return.
     */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 10;
    /**
     * The default timeout.
     */
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int PASSWORD_STATUS_PASS = -1;
    /**
     * The list of valid scope values.
     */
    private static final int[] VALID_SCOPE_VALUES = new int[]{SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE};
    /**
     * The filter path to the lookup value of the user.
     */
    private String filter;
    /**
     * Whether the LdapTemplate should ignore partial results.
     */
    private boolean ignorePartialResultException = false;
    /**
     * Context to authenticate user to LDAP.
     */
    private ContextSource context;
    /**
     * LdapTemplate to execute ldap queries.
     */
    private LdapTemplate ldapTemplate;
    /**
     * The maximum number of results to return.
     */
    private int maxNumberResults = LdapPpolicyPasswordPolicyEnforcer.DEFAULT_MAX_NUMBER_OF_RESULTS;
    /**
     * The scope.
     */
    private int scope = SearchControls.SUBTREE_SCOPE;
    /**
     * The search base to find the user under.
     */
    private String searchBase;
    /**
     * The amount of time to wait.
     */
    private int timeout = LdapPpolicyPasswordPolicyEnforcer.DEFAULT_TIMEOUT;
    /**
     * default number of days that a warning message will be displayed
     */
    private int warningDays = 30;
    /**
     * The attribute that contains the number of days the user's password is
     * valid
     */
    protected int validDays;
    /**
     * Disregard WarnPeriod and warn all users of password expiration
     */
    protected Boolean warnAll = Boolean.FALSE;
    /**
     * Skip warning period check from ldap policy, use configured value instead
     * (
     */
    private Boolean ignoreLDAPWarningDays = Boolean.FALSE;
    
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ldapTemplate, "ldapTemplate cannot be null");
        Assert.notNull(this.filter, "filter cannot be null");
        Assert.notNull(this.searchBase, "searchBase cannot be null");
        Assert.notNull(this.warnAll, "warnAll cannot be null");
        Assert.isTrue(this.filter.contains("%u") || this.filter.contains("%U"), "filter must contain %u");

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);

        for (final int element : VALID_SCOPE_VALUES) {
            if (this.scope == element) {
                return;
            }
        }
        throw new IllegalStateException("You must set a valid scope. Valid scope values are: " + Arrays.toString(VALID_SCOPE_VALUES));
    }

    /**
     * Method to set the data source and generate a LDAPTemplate.
     *
     * @param dataSource the data source to use.
     */
    public void setContextSource(final ContextSource contextSource) {
        this.context = contextSource;
        this.ldapTemplate = new LdapTemplate(contextSource);
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
     * @param ValidDaysAttribute The ValidDaysAttribute to set.
     */
    public void setValidDays(final int validDays) {
        this.validDays = validDays;
        logDebug("Valid days: " + validDays);
    }

    /**
     * @param warnAll Disregard warningPeriod and warn all users of password
     * expiration.
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

    public long getNumberOfDaysToPasswordExpirationDate(final String userId, final String credentials) throws LdapPasswordPolicyEnforcementException {

        // first get the right dn for the user id form ldap
        String cn = this.getFullDNFromUserId(userId);

        DirContext authenticatedUserContext = context.getContext(cn, credentials);
        PasswordPolicyResponseControl p = PasswordPolicyControlExtractor.extractControl(authenticatedUserContext);
        int daysToExpire;
        if (p != null) {
            // figure out how long the password is still valid for
            int seconds = p.getTimeBeforeExpiration();
            daysToExpire = ((seconds / 60) / 60) / 24;
            // then get the number of days the warning message should be displayed from before expiry

        } else {
            daysToExpire = this.validDays;
        }
        if (this.displayWarning(p, daysToExpire) || this.warnAll) {
            return daysToExpire;
        } else {
            return -1;
        }
    }

    private void logDebug(final String log) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(log);
        }
    }

    private void logError(final String log, final Exception e) {
        if (this.logger.isErrorEnabled()) {
            this.logger.error(e.getMessage(), e);
        }
    }

    private void logInfo(final String log) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info(log);
        }
    }

    private SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(new String[0]);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    /**
     * @return the principalNameTransformer
     */
    public PrincipalNameTransformer getPrincipalNameTransformer() {
        return principalNameTransformer;
    }

    /**
     * @param principalNameTransformer the principalNameTransformer to set
     */
    public void setPrincipalNameTransformer(PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    private String getFullDNFromUserId(String userId) throws LdapPasswordPolicyEnforcementException {
        // first get the right dn for the user id form ldap
        final List<String> cns = new ArrayList<String>();

        final SearchControls searchControls = this.getSearchControls();

        final String base = this.searchBase;
        final String transformedUsername = getPrincipalNameTransformer().transform(userId);
        final String searchFilter = LdapUtils.getFilterWithValues(this.filter, transformedUsername);
        this.ldapTemplate.search(
                new SearchExecutor() {
                    public NamingEnumeration executeSearch(final DirContext context) throws NamingException {
                        return context.search(base, searchFilter, searchControls);
                    }
                },
                new NameClassPairCallbackHandler() {
                    public void handleNameClassPair(final NameClassPair nameClassPair) {
                        cns.add(nameClassPair.getNameInNamespace());
                    }
                });

        if (cns.isEmpty()) {
            String logMessage = "Search for " + filter + " returned 0 results.";
            logger.info(logMessage);
            throw new LdapPasswordPolicyEnforcementException(logMessage);
        }
        if (cns.size() > 1) {
            String logMessage = "Search for " + filter + " returned multiple results, which is not allowed.";
            logger.warn(logMessage);
            throw new LdapPasswordPolicyEnforcementException(logMessage);
        }
        return cns.get(0);
    }

    private boolean displayWarning(PasswordPolicyResponseControl p, int expiresIn) {
        // ski[p the LDAP policy check if configured to do so (expensive action)
        if (this.isIgnoreLDAPWarningDays()) {
            Calendar warnWhen = new GregorianCalendar();
            warnWhen.add(Calendar.DAY_OF_YEAR, expiresIn);
            warnWhen.add(Calendar.DAY_OF_YEAR, -1 * this.warningDays);
            return warnWhen.before(new GregorianCalendar());
        } else {
            return p.hasWarning();
        }
    }

    /**
     * @return the ignoreLDAPWarningDays
     */
    public Boolean isIgnoreLDAPWarningDays() {
        return ignoreLDAPWarningDays;
    }

    /**
     * @param ignoreLDAPWarningDays the ignoreLDAPWarningDays to set
     */
    public void setIgnoreLDAPWarningDays(Boolean ignoreLDAPWarningDays) {
        this.ignoreLDAPWarningDays = ignoreLDAPWarningDays;
    }
}
