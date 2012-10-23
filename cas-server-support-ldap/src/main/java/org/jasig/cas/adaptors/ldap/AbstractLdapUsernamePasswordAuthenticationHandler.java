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
import java.util.List;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.ContextSource;
import org.springframework.util.Assert;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Abstract class to handle common LDAP functionality.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractLdapUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {
    
    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The ldap query search result on the current transformed username. Contains attributes defined to be returned by the query, if any. **/
    private SearchResult authenticatedDistinguishedNameSearchResult = null;
    
    /** 
     * The ldap query scope. 
     *  @see #getSearchControls()
     */
    @Min(0)
    @Max(2)
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The search base to find the user under. */
    private String searchBase;
    
    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;
    
    /** {@link LdapTemplate} to execute ldap queries. */
    @NotNull
    private LdapTemplate ldapTemplate;
    
    /** Instance of {@link ContextSource} */
    @NotNull
    private ContextSource contextSource;

    /** The filter path to the uid of the user. */
    @NotNull
    private String filter;
    
    
    /** Whether the LdapTemplate should ignore partial results. */
    private boolean ignorePartialResultException = false;

    /**
     * List of attributes to return as part of the ldap query
     * @see #getSearchControls()
     */
    @NotNull
    private List<String> attributesToReturn = new ArrayList<String>();
    
    /**
     * Method to set the data source and generate a JdbcTemplate.
     * 
     * @param contextSource the data source to use.
     */
    public final void setContextSource(final ContextSource contextSource) {
        this.contextSource = contextSource;
    }
    
    public final void setIgnorePartialResultException(final boolean ignorePartialResultException) {
        this.ignorePartialResultException = ignorePartialResultException;
    }
    
    /** 
     * Allows to set the ldap query search result on the current transformed username. 
     * Contains attributes defined to be returned by the query, if any. Subclasses can choose
     * to set this property when locating the user in the directory. 
     * 
     * @see BindLdapAuthenticationHandler
     * @see FastBindLdapAuthenticationHandler
     * @see #authenticatedDistinguishedNameSearchResult
     **/
    protected void setAuthenticatedDistinguishedNameSearchResult(final SearchResult dn) {
        this.authenticatedDistinguishedNameSearchResult = dn;
    }
    
    /** 
     * Allows to return the ldap query search result on the current transformed username. 
     * Contains attributes defined to be returned by the query, if any. 
     * @see BindLdapAuthenticationHandler
     * @see FastBindLdapAuthenticationHandler
     * @see #authenticatedDistinguishedNameSearchResult
     **/
    public SearchResult getAuthenticatedDistinguishedNameSearchResult() {
        return this.authenticatedDistinguishedNameSearchResult;
    }
    
    /**
     * Specifies the search criteria to be used by the ldap query
     * @return an instance of {@link SearchControls} that encapsulated the query configuration. 
     */
    protected SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        constraints.setSearchScope(this.scope);

        final String[] attr = this.getAttributesToReturn().toArray(new String[this.getAttributesToReturn().size()]);        
        constraints.setReturningAttributes(attr);
        
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);
        return constraints;
    }
    
    /**
     * Method to return the search base.
     * @return the search base.
     */
    protected String getSearchBase() {
        return this.searchBase;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public final void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
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
     * @param maxNumberResults The maxNumberResults to set.
     */
    public final void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param timeout The timeout to set.
     */
    public final void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * Method to return the LdapTemplate
     * 
     * @return a fully created LdapTemplate.
     */
    protected final LdapTemplate getLdapTemplate() {
        return this.ldapTemplate;
    }

    protected final ContextSource getContextSource() {
        return this.contextSource;
    }

    protected final String getFilter() {
        return this.filter;
    }
    
    /**
     * 
     * @return an array of attribute names that are to be returned by the ldap query
     */
    public final List<String> getAttributesToReturn() {
        return this.attributesToReturn;
    }

    /**
     * Specify the list of attribute names to be returned by the ldap query.
     * @param attr attribute names to return
     */
    public final void setAttributesToReturn(final List<String> attr) {
        this.attributesToReturn = attr;
    }
    
    @Override
    public final void afterPropertiesSet() throws Exception {
        Assert.isTrue(this.filter.contains("%u") || this.filter.contains("%U"), "filter must contain %u or %U");

        if (this.ldapTemplate == null) {
            this.ldapTemplate = new LdapTemplate(this.contextSource);
        }

        this.ldapTemplate.setIgnorePartialResultException(this.ignorePartialResultException);
        afterPropertiesSetInternal();
    }

    /**
     * Available ONLY for subclasses that are doing special things with the ContextSource.
     *
     * @param ldapTemplate the LDAPTemplate to use.
     */
    protected final void setLdapTemplate(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    protected void afterPropertiesSetInternal() throws Exception {
        // template method with nothing to do for sub classes.
    }

    /**
     * @param filter The filter to set.
     */
    public final void setFilter(final String filter) {
        this.filter = filter;
    }

    
}
