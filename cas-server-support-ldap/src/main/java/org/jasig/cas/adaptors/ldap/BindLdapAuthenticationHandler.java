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

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs LDAP authentication via two distinct steps:
 *  <ol>
 *  <li>Search for an LDAP DN using arbitrary search filter.</li>
 *  <li>Bind using DN in first step with password provided by login Webflow.</li>
 *  </ol>
 *  <p>
 *  The search step is typically performed anonymously or using a constant
 *  authenticated context such as an administrator username/password or client
 *  certificate.  This step is suitable for LDAP connection pooling to improve
 *  efficiency and performance.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public class BindLdapAuthenticationHandler extends AbstractLdapUsernamePasswordAuthenticationHandler {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** The search base to find the user under. */
    private String searchBase;

    /** The scope. */
    @Min(0)
    @Max(2)
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;

    /** Boolean of whether multiple accounts are allowed. */
    private boolean allowMultipleAccounts;

    protected final boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {

        final List<String> cns = new ArrayList<String>();
        
        final SearchControls searchControls = getSearchControls();
        
        final String base = this.searchBase;
        final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
        final String filter = LdapUtils.getFilterWithValues(getFilter(), transformedUsername);
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
            log.info("Search for " + filter + " returned 0 results.");
            return false;
        }
        if (cns.size() > 1 && !this.allowMultipleAccounts) {
            log.warn("Search for " + filter + " returned multiple results, which is not allowed.");
            return false;
        }

        for (final String dn : cns) {
            DirContext test = null;
            String finalDn = composeCompleteDnToCheck(dn, credentials);
            try {
                this.log.debug("Performing LDAP bind with credential: " + dn);
                test = this.getContextSource().getContext(
                    finalDn,
                    getPasswordEncoder().encode(credentials.getPassword()));

                if (test != null) {
                    return true;
                }
            } catch (final Exception e) {
                if (this.log.isErrorEnabled())
                    this.log.error(e.getMessage(), e);

                throw handleLdapError(e);
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

    private SearchControls getSearchControls() {
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

    /**
     * Sets the context source for LDAP searches.  This method may be used to
     * support use cases like the following:
     * <ul>
     * <li>Pooling of LDAP connections used for searching (e.g. via instance
     * of {@link org.springframework.ldap.pool.factory.PoolingContextSource}).</li>
     * <li>Searching with client certificate credentials.</li>
     * </ul>
     * <p>
     * If this is not defined, the context source defined by
     * {@link #setContextSource(ContextSource)} is used.
     *
     * @param contextSource LDAP context source.
     */
    public final void setSearchContextSource(final ContextSource contextSource) {
        setLdapTemplate(new LdapTemplate(contextSource));
    }
}