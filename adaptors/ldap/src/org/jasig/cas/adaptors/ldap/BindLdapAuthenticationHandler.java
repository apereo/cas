/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
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

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.AttributeHelper;
import org.springframework.ldap.core.SearchResultCallbackHandler;
import org.springframework.ldap.core.support.LdapDaoSupport;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class BindLdapAuthenticationHandler extends LdapDaoSupport implements AuthenticationHandler {

    private static final String[] RETURN_VALUES = new String[] {"cn"};

    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    private static final int DEFAULT_TIMEOUT = 1000;

    private String searchBase;

    private boolean scopeOneLevel;

    private boolean scopeObject;

    private boolean scopeSubtree;

    private String filter;

    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    private int timeout = DEFAULT_TIMEOUT;

    private int scopeValue;

    private boolean allowMultipleAccounts;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;

        List values = (List)this.getLdapTemplate().search(this.searchBase, LdapUtils.getFilterWithValues(this.filter, uRequest.getUserName()),
            this.getSearchControls(), new SearchResultCallbackHandler(){

                private List cns = new ArrayList();

                public void processSearchResult(SearchResult searchResult) throws NamingException {
                    this.cns.add(AttributeHelper.getAttributeAsString(searchResult, "cn"));
                }

                public Object getResult() {
                    return this.cns;
                }
            });

        if (values == null || values.isEmpty())
            return false;

        if (values.size() > 1 && !this.allowMultipleAccounts)
            return false;

        for (Iterator iter = values.iterator(); iter.hasNext();) {
            String dn = (String)iter.next();

            DirContext test = this.getContextSource().getDirContext(dn + "," + this.searchBase, uRequest.getPassword());

            if (test != null) {
                org.springframework.ldap.support.LdapUtils.closeContext(test);
                return true;
            }
        }

        return false;
    }

    protected SearchControls getSearchControls() {
        SearchControls constraints = new SearchControls(this.scopeValue, this.maxNumberResults, this.timeout, RETURN_VALUES, false, false);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

        return constraints;
    }

    /**
     * @see org.springframework.ldap.core.support.LdapDaoSupport#initDao()
     */
    public void initDao() throws Exception {
        if (!this.scopeOneLevel && !this.scopeObject && !this.scopeSubtree)
            throw new IllegalStateException("Either scopeOneLevel, scopeObject or scopeSubtree must be set to true on " + this.getClass().getName());

        if ((this.scopeOneLevel && this.scopeObject) || (this.scopeOneLevel && this.scopeSubtree) || (this.scopeObject && this.scopeSubtree))
            throw new IllegalStateException("You can only set one property to tree on scopeOneLevel, scopeObject, and scopeSubtree for "
                + this.getClass().getName());

        if (this.scopeOneLevel)
            this.scopeValue = SearchControls.ONELEVEL_SCOPE;
        else if (this.scopeObject)
            this.scopeValue = SearchControls.OBJECT_SCOPE;
        else
            this.scopeValue = SearchControls.SUBTREE_SCOPE;
    }

    public boolean supports(Credentials credentials) {
        return credentials != null && credentials.getClass().equals(UsernamePasswordCredentials.class);
    }

    /**
     * @param allowMultipleAccounts The allowMultipleAccounts to set.
     */
    public void setAllowMultipleAccounts(boolean allowMultipleAccounts) {
        this.allowMultipleAccounts = allowMultipleAccounts;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public void setMaxNumberResults(int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param scopeObject The scopeObject to set.
     */
    public void setScopeObject(boolean scopeObject) {
        this.scopeObject = scopeObject;
    }

    /**
     * @param scopeOneLevel The scopeOneLevel to set.
     */
    public void setScopeOneLevel(boolean scopeOneLevel) {
        this.scopeOneLevel = scopeOneLevel;
    }

    /**
     * @param scopeSubtree The scopeSubtree to set.
     */
    public void setScopeSubtree(boolean scopeSubtree) {
        this.scopeSubtree = scopeSubtree;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }
}
