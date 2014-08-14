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
package org.jasig.cas.authentication.support;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.auth.AuthenticationCriteria;
import org.ldaptive.auth.SearchEntryResolver;

import java.util.Arrays;

/**
 * Ldaptive extension component for Active Directory that supports querying for an entry by User Principal Name (UPN).
 * This component only provides meaningful results when used on a bound connection; therefore it cannot be used with
 * ldaptive support for the AD FastBind operation, <code>org.ldaptive.ad.extended.FastBindOperation</code>.
 * <p>
 * Since the UPN is abstracted from the location of an entry in the directory, subtree searching is required to
 * locate an entry. The {@link #setBaseDn(String)} property must be set to the lowest common branch where all
 * authenticated users are located, commonly <code>dc=example,dc=org</code> or <code>OU=Users,dc=example,dc=org</code>.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class UpnSearchEntryResolver extends SearchEntryResolver {

    /** UPN-based search filter. */
    private static final String DEFAULT_SEARCH_FILTER = "userPrincipalName={user}";

    /** Base DN of LDAP subtree search. */
    private String baseDn;

    private SearchScope searchScope = SearchScope.SUBTREE;

    private boolean resolveSearchEntryByFullDn = true;

    private String searchFilter = DEFAULT_SEARCH_FILTER;

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }

    /**
     * Sets the base DN used for the subtree search for LDAP entry.
     *
     * @param dn Subtree search base DN.
     */
    public void setBaseDn(final String dn) {
        this.baseDn = dn;
    }

    public void setSearchScope(final SearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public void setResolveSearchEntryByFullDn(final boolean resolveSearchEntryByFullDn) {
        this.resolveSearchEntryByFullDn = resolveSearchEntryByFullDn;
    }

    @Override
    public SearchResult performLdapSearch(final Connection conn, final AuthenticationCriteria ac) throws LdapException {
        final SearchResult result = super.performLdapSearch(conn, ac);
        if (result.getEntries().size() == 0) {
            logger.warn("Unable to find any entries after the search for {}", ac.getDn());
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected SearchRequest createSearchRequest(final AuthenticationCriteria ac) {

        final SearchRequest sr = new SearchRequest();
        sr.setSearchScope(this.searchScope);
        sr.setBaseDn(this.baseDn);

        final SearchFilter filter = new SearchFilter(this.searchFilter);
        if (this.resolveSearchEntryByFullDn) {
            filter.setParameter("user", ac.getDn());
        } else {
            filter.setParameter("user", ac.getAuthenticationRequest().getUser());
        }
        sr.setSearchFilter(filter);

        sr.setSearchEntryHandlers(getSearchEntryHandlers());
        sr.setReturnAttributes(ac.getAuthenticationRequest().getReturnAttributes());

        logger.debug("Searching entries by [{}] to return attributes [{}]",
                sr.getSearchFilter().format(), Arrays.toString(sr.getReturnAttributes()));

        return sr;
    }
}
