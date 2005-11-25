/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jasig.cas.adaptors.ldap.util.LdapUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.ldap.core.SearchResultCallbackHandler;

/**
 * Handler to do LDAP bind.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BindLdapAuthenticationHandler extends
    AbstractLdapUsernamePasswordAuthenticationHandler {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The list of valid scope values. */
    private static final int[] VALID_SCOPE_VALUES = new int[] {
        SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE};

    /** The search base to find the user under. */
    private String searchBase;

    /** The scope. */
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

        final List values = (List) this.getLdapTemplate().search(
            this.searchBase,
            LdapUtils.getFilterWithValues(getFilter(), credentials
                .getUsername()), this.getSearchControls(),
            new SearchResultCallbackHandler(){

                private final List cns = new ArrayList();

                public void processSearchResult(final SearchResult searchResult)
                    throws NamingException {
                    this.cns.add(searchResult.getName());
                }

                public Object getResult() {
                    return this.cns;
                }
            });

        if (values == null || values.isEmpty()
            || (values.size() > 1 && !this.allowMultipleAccounts)) {
            return false;
        }

        for (final Iterator iter = values.iterator(); iter.hasNext();) {
            final String dn = (String) iter.next();

            DirContext test = null;
            try {
                test = this.getContextSource().getDirContext(
                    composeCompleteDnToCheck(dn, credentials),
                    credentials.getPassword());

                if (test != null) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            } finally {
                org.springframework.ldap.support.LdapUtils.closeContext(test);
            }
        }

        return false;
    }

    protected String composeCompleteDnToCheck(final String dn,
        final UsernamePasswordCredentials credentials) {
        return dn + "," + this.searchBase;
    }

    private final SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(new String[0]);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    protected final void initDao() throws Exception {
        for (int i = 0; i < VALID_SCOPE_VALUES.length; i++) {
            if (this.scope == VALID_SCOPE_VALUES[i]) {
                return;
            }
        }

        throw new IllegalStateException("You must set a scope.");
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