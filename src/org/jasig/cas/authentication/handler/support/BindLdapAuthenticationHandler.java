/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.util.LdapUtils;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class BindLdapAuthenticationHandler extends AbstractLdapAuthenticationHandler {

    private static final String[] RETURN_VALUES = new String[] {"cn"};

    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    private static final int DEFAULT_TIMEOUT = 1000;

    private String bindUsername;

    private String bindPassword;

    private String searchBase;

    private boolean scopeOneLevel;

    private boolean scopeObject;

    private boolean scopeSubtree;

    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    private int timeout = DEFAULT_TIMEOUT;

    private SearchControls constraints;

    private int scopeValue;

    private boolean allowMultipleAccounts;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticate(final AuthenticationRequest request) {
        final UsernamePasswordAuthenticationRequest uRequest = (UsernamePasswordAuthenticationRequest)request;

        for (Iterator iter = this.getServers().iterator(); iter.hasNext();) {
            DirContext dirContext = null;
            final String url = (String)iter.next();
            final List results = new ArrayList();

            try {
                dirContext = this.getContext(this.bindUsername, this.bindPassword, url);

                if (dirContext == null) {
                    log.debug("Unable to authenticate LDAP user [" + this.bindUsername + "] for LDAP server [" + url + "]");
                    return false;
                }

                NamingEnumeration namingEnumeration = dirContext.search(this.searchBase, LdapUtils.getFilterWithValues(this.getFilter(), uRequest
                    .getUserName()), this.constraints);

                if (!namingEnumeration.hasMoreElements()) {
                    dirContext.close();
                    return false;
                }

                while (namingEnumeration.hasMoreElements()) {
                    String dn = (String)namingEnumeration.next();
                    results.add(dn);
                }

                dirContext.close();

                if (results.size() > 1 && !allowMultipleAccounts) {
                    return false;
                }

                for (Iterator resultsIter = results.iterator(); iter.hasNext();) {
                    String dn = (String)resultsIter.next();

                    DirContext test = this.getContext(dn + "," + this.searchBase, uRequest.getPassword(), url);

                    if (test != null) {
                        test.close();
                        return true;
                    }
                }

                return false;
            }
            catch (NamingException e) {
                log.debug("LDAP ERROR: Unable to connect to LDAP server " + url + ".  Attempting to contact next server (if exists).");
            }
        }

        return false;
    }

    protected SearchControls initializeSearchControls() {
        SearchControls constraints = new SearchControls(this.scopeValue, this.maxNumberResults, this.timeout, RETURN_VALUES, false, false);
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE); // TODO why????

        return constraints;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.bindUsername == null || this.bindPassword == null)
            throw new IllegalStateException("bindUsername and bindPassword must be set on " + this.getClass().getName());

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

        this.constraints = initializeSearchControls();
    }

    /**
     * @param allowMultipleAccounts The allowMultipleAccounts to set.
     */
    public void setAllowMultipleAccounts(boolean allowMultipleAccounts) {
        this.allowMultipleAccounts = allowMultipleAccounts;
    }

    /**
     * @param bindPassword The bindPassword to set.
     */
    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    /**
     * @param bindUsername The bindUsername to set.
     */
    public void setBindUsername(String bindUsername) {
        this.bindUsername = bindUsername;
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
}
