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
package org.jasig.cas.authentication.principal;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.DerefAliases;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.SortBehavior;
import org.ldaptive.handler.SearchEntryHandler;

/**
 * @author Scott Battaglia
 * @since 3.2.1
 */
public abstract class AbstractLdapPersonDirectoryCredentialsToPrincipalResolver extends
AbstractPersonDirectoryCredentialsToPrincipalResolver {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 2;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** LdapTemplate to execute ldap queries. */
    @NotNull
    private ConnectionFactory connectionFactory;

    /** Time out. **/
    private int timeout = DEFAULT_TIMEOUT;
    
    /** The attribute that contains the value that should become the principal. */
    @NotNull
    private String[] attributeIds;

    /** The search base to find the user under. */
    @NotNull
    private String searchBase;

    private SearchFilter searchFilter;

    private boolean followReferrals = true;

    private long size = DEFAULT_MAX_NUMBER_OF_RESULTS;

    private SortBehavior sortBehavior = SortBehavior.getDefaultSortBehavior();

    private SearchScope searchScope = SearchScope.SUBTREE;

    private List<SearchEntryHandler> searchEntryHandlers = Collections.emptyList();

    private DerefAliases derefAliases = DerefAliases.ALWAYS;

    /**
     * Method to set the datasource and generate a LDAPTemplate.
     * @param connectionFactory the datasource to use.
     */
    public final void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public final void setFilter(final String filter, final Object... parameters) {
        this.searchFilter = new SearchFilter(filter, parameters);
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

    protected final String[] getAttributeIds() {
        return this.attributeIds;
    }

    protected final String getSearchBase() {
        return this.searchBase;
    }

    protected final int getTimeout() {
        return this.timeout;
    }

    protected final ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
    
    protected final SearchFilter getSearchFilter() {
        return this.searchFilter;
    }
    
    protected final SearchRequest getSearchRequest() {
        final SearchRequest request = new SearchRequest();
        request.setFollowReferrals(getFollowReferrals());
        request.setBaseDn(getSearchBase());
        request.setSearchFilter(getSearchFilter());
        request.setReturnAttributes(getAttributeIds());
        request.setTimeLimit(getTimeout());
        request.setSizeLimit(getSize());
        request.setSortBehavior(getSortBehavior());
        request.setSearchScope(getSearchScope());
        request.setDerefAliases(getDerefAliases());
        final SearchEntryHandler[] handlers = getSearchEntryHandlers().toArray(new SearchEntryHandler[] {});
        request.setSearchEntryHandlers(handlers);
        
        return request;
    }

    protected final DerefAliases getDerefAliases() {
        return this.derefAliases;
    }

    protected final List<SearchEntryHandler> getSearchEntryHandlers() {
        return this.searchEntryHandlers;
    }

    protected final SearchScope getSearchScope() {
        return this.searchScope;
    }

    protected final SortBehavior getSortBehavior() {
        return this.sortBehavior;
    }

    protected final long getSize() {
        return this.size;
    }

    protected final boolean getFollowReferrals() {        
        return this.followReferrals;
    }
    
    public final void setAttributeIds(String[] attributeIds) {
        this.attributeIds = attributeIds;
    }

    public final void setSearchFilter(SearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    public final void setFollowReferrals(boolean followReferrals) {
        this.followReferrals = followReferrals;
    }

    public final void setSize(long size) {
        this.size = size;
    }

    public final void setSortBehavior(SortBehavior sortBehavior) {
        this.sortBehavior = sortBehavior;
    }

    public final void setSearchScope(SearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public final void setSearchEntryHandlers(List<SearchEntryHandler> searchEntryHandlers) {
        this.searchEntryHandlers = searchEntryHandlers;
    }

    public final void setDerefAliases(DerefAliases derefAliases) {
        this.derefAliases = derefAliases;
    }

}
