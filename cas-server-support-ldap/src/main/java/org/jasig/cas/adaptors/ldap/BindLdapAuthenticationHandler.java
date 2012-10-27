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
import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * @since 3.0.3
 */
public class BindLdapAuthenticationHandler extends AbstractLdapUsernamePasswordAuthenticationHandler {

    /** Boolean of whether multiple accounts are allowed. */
    private boolean allowMultipleAccounts;

    protected final boolean authenticateUsernamePasswordInternal(final UsernamePasswordCredentials credentials) throws AuthenticationException {

        final Map<String, SearchResult> cns = new HashMap<String, SearchResult>();
        final SearchControls searchControls = getSearchControls();
        final String transformedUsername = getPrincipalNameTransformer().transform(credentials.getUsername());
        final String filter = LdapUtils.getFilterWithValues(getFilter(), transformedUsername);
        
        this.getLdapTemplate().search(
            new SearchExecutor() {
                public NamingEnumeration<?> executeSearch(final DirContext context) throws NamingException {
                    return context.search(BindLdapAuthenticationHandler.this.getSearchBase(), filter, searchControls);
                }
            },
            new NameClassPairCallbackHandler(){
                public void handleNameClassPair(final NameClassPair nameClassPair) {
                    final SearchResult result = (SearchResult) nameClassPair;
                    cns.put(result.getNameInNamespace(), result);
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

        final Iterator<String> setOfCns = cns.keySet().iterator();
        
        while (setOfCns.hasNext()) {
            DirContext test = null;

            try {
                final String dn = setOfCns.next();
                final String finalDn = composeCompleteDnToCheck(dn, credentials);
                
                log.debug("Performing LDAP bind with credential: " + dn);
                test = this.getContextSource().getContext(finalDn, getPasswordEncoder().encode(credentials.getPassword()));
                if (test != null) {
                    setAuthenticatedDistinguishedNameSearchResult(cns.get(dn));
                    return true;
                }
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
                throw new LdapAuthenticationException(e);
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

    /**
     * Method to return whether multiple accounts are allowed.
     * @return true if multiple accounts are allowed, false otherwise.
     */
    protected boolean isAllowMultipleAccounts() {
        return this.allowMultipleAccounts;
    }
    
    /**
     * @param allowMultipleAccounts The allowMultipleAccounts to set.
     */
    public void setAllowMultipleAccounts(final boolean allowMultipleAccounts) {
        this.allowMultipleAccounts = allowMultipleAccounts;
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