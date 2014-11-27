/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.auth.AuthenticationCriteria;
import org.ldaptive.auth.SearchEntryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated As of 4.1, this component is no longer required. Ldaptive's own {@link SearchEntryResolver} now supports
 * all the functionality that is presented by this class through various settings and filter parameters. This class
 * is scheduled to be removed in future CAS versions.
 *
 * Ldaptive extension component for Active Directory that supports querying for an entry by User Principal Name (UPN).
 * This component only provides meaningful results when used on a bound connection; therefore it cannot be used with
 * ldaptive support for the AD FastBind operation, <code>org.ldaptive.ad.extended.FastBindOperation</code>.
 * <p>
 * Since the UPN is abstracted from the location of an entry in the directory, subtree searching is required to
 * locate an entry. The {@link #setBaseDn(String)} property must be set to the lowest common branch where all
 * authenticated users are located, commonly <code>dc=example,dc=org</code> or <code>OU=Users,dc=example,dc=org</code>.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Deprecated
public final class UpnSearchEntryResolver extends SearchEntryResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnSearchEntryResolver.class);

    /** UPN-based search filter. */
    private static final String SEARCH_FILTER = "userPrincipalName={0}";

    /** Base DN of LDAP subtree search. */
    private String baseDn;

    /**
     * Instantiates a new Search entry resolver.
     */
    public UpnSearchEntryResolver() {
        super();
        LOGGER.warn("UpnSearchEntryResolver will be removed in future CAS versions. Consider using the SearchEntryResolver directly");
    }
    /**
     * Sets the base DN used for the subtree search for LDAP entry.
     *
     * @param dn Subtree search base DN.
     */
    public void setBaseDn(final String dn) {
        this.baseDn = dn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SearchRequest createSearchRequest(final AuthenticationCriteria ac) {
        final SearchRequest sr = new SearchRequest();
        sr.setSearchScope(SearchScope.SUBTREE);
        sr.setBaseDn(this.baseDn);
        sr.setSearchFilter(new SearchFilter(SEARCH_FILTER, new Object[]{ac.getDn()}));
        sr.setSearchEntryHandlers(getSearchEntryHandlers());
        sr.setReturnAttributes(ac.getAuthenticationRequest().getReturnAttributes());
        return sr;
    }
}
