/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.ldap;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;
import org.inspektr.common.ioc.annotation.IsIn;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler to do LDAP bind.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public class BindLdapAuthenticationHandler extends
    AbstractLdapUsernamePasswordAuthenticationHandler {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The search base to find the user under. */
    private String searchBase;

    /** The scope. */
    @IsIn({SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE})
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    /** Boolean of whether multiple accounts are allowed. */
    private boolean allowMultipleAccounts;

    protected final boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException {

        final List<String> cns = new ArrayList<String>();
        
        final SearchControls searchControls = getSearchControls();
        
        final String base = this.searchBase;
        final String filter = LdapUtils.getFilterWithValues(getFilter(), credentials.getUsername());
        this.getLdapTemplate().search(
            new SearchExecutor() {

                public NamingEnumeration executeSearch(final DirContext context) throws NamingException {
                    return context.search(base, filter, searchControls);
                }
            },
            new NameClassPairCallbackHandler(){

                public void handleNameClassPair(final NameClassPair nameClassPair) {
                    cns.add(nameClassPair.getNameInNamespace());
                }
            });
        
        if (cns.isEmpty()) {
            this.log.info("Search for " + filter + " returned 0 results.");
            return false;
        }
        if (cns.size() > 1 && !this.allowMultipleAccounts) {
            this.log.warn("Search for " + filter + " returned multiple results, which is not allowed.");
            return false;
        }
        
        for (final String dn : cns) {
            DirContext test = null;
            String finalDn = composeCompleteDnToCheck(dn, credentials);
            try {
                this.log.debug("Performing LDAP bind with credential: " + dn);
                test = this.getContextSource().getContext(
                    finalDn,
                    credentials.getPassword());

                if (test != null) {
                    return true;
                }
            } catch (final Exception e) {
                // if we catch an exception, just try the next cn
            } finally {
                LdapUtils.closeContext(test);
            }
        }

        return false;
    }

    protected String composeCompleteDnToCheck(final String dn,
        final UsernamePasswordCredentials credentials) {
        return dn;
    }

    private final SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(new String[0]);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    /**
     * Method to return whether multiple accounts are allowed.
     * @return true if multiple accounts are allowed, false otherwise.
     */
    protected boolean isAllowMultipleAccounts() {
        return this.allowMultipleAccounts;
    }

    /**
     * Method to return the max number of results allowed.
     * @return the maximum number of results.
     */
    protected int getMaxNumberResults() {
        return this.maxNumberResults;
    }

    /**
     * Method to return the scope.
     * @return the scope
     */
    protected int getScope() {
        return this.scope;
    }

    /**
     * Method to return the search base.
     * @return the search base.
     */
    protected String getSearchBase() {
        return this.searchBase;
    }

    /**
     * Method to return the timeout. 
     * @return the timeout.
     */
    protected int getTimeout() {
        return this.timeout;
    }

    public final void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param allowMultipleAccounts The allowMultipleAccounts to set.
     */
    public void setAllowMultipleAccounts(final boolean allowMultipleAccounts) {
        this.allowMultipleAccounts = allowMultipleAccounts;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public final void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public final void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    /**
     * @param timeout The timeout to set.
     */
    public final void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
}