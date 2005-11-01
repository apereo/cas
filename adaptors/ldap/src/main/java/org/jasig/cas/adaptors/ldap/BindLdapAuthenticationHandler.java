/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
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
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.ldap.core.SearchResultCallbackHandler;
import org.springframework.ldap.core.support.LdapDaoSupport;

/**
 * Handler to do LDAP bind.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class BindLdapAuthenticationHandler extends LdapDaoSupport implements
    AuthenticationHandler {

    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    private static final int DEFAULT_TIMEOUT = 1000;

    private static final int[] VALID_SCOPE_VALUES = new int[] {
        SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE};

    private String searchBase;

    private int scope = SearchControls.SUBTREE_SCOPE;

    private String filter;

    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    private int timeout = DEFAULT_TIMEOUT;

    private boolean allowMultipleAccounts;

    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials) request;

        final List values = (List) this.getLdapTemplate().search(
            this.searchBase,
            LdapUtils.getFilterWithValues(this.filter, uRequest.getUsername()),
            this.getSearchControls(), new SearchResultCallbackHandler(){

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
                    dn + "," + this.searchBase, uRequest.getPassword());

                if (test != null) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
                finally {
                org.springframework.ldap.support.LdapUtils.closeContext(test);
            }
        }

        return false;
    }

    protected SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(new String[0]);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);

        return constraints;
    }

    protected void initDao() throws Exception {
        for (int i = 0; i < VALID_SCOPE_VALUES.length; i++) {
            if (this.scope == VALID_SCOPE_VALUES[i]) {
                return;
            }
        }

        throw new IllegalStateException("You must set a scope.");
    }

    public boolean supports(Credentials credentials) {
        return credentials != null
            && credentials.getClass().equals(UsernamePasswordCredentials.class);
    }

    public void setScope(final int scope) {
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
    public void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }
}