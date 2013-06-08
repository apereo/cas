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
package org.jasig.cas.adaptors.ldap.services;

import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.jasig.cas.util.LdapUtils;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DerefAliases;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.SortBehavior;
import org.ldaptive.cache.Cache;
import org.ldaptive.handler.SearchEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the ServiceRegistryDao interface which stores the services in a LDAP Directory.
 * @author Misagh Moayyed
 */
public final class LdapServiceRegistryDao implements ServiceRegistryDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private ConnectionFactory connectionFactory;

    @NotNull
    private LdapRegisteredServiceMapper ldapServiceMapper = new DefaultLdapServiceMapper();

    private Cache<SearchRequest> cacheStrategy = null;
    private String baseDn = "";
    private int sizeLimit = 0;
    private DerefAliases derefAliases = DerefAliases.ALWAYS;
    private boolean followReferrals = true;
    private boolean typesOnly = false;
    private SearchScope searchScope = SearchScope.SUBTREE;
    private SortBehavior sortBehavior = SortBehavior.getDefaultSortBehavior();
    private SearchEntryHandler[] searchEntryHandlers;
    private String searchFilter = null;
    private String loadFilter = null;
    private String[] attributesToReturn = null;
    private int timeLimit = 0;

    @Override
    public RegisteredService save(final RegisteredService rs) {
        if (rs.getId() != -1) {
            return update(rs);
        }

        Connection connection = null;
        try {
            connection = this.connectionFactory.getConnection();
            final AddOperation operation = new AddOperation(connection);

            final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
            operation.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            return rs;
        } catch (final LdapException e) {
            log.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }
        return null;
    }

    private RegisteredService update(final RegisteredService rs) {
        Connection connection = null;
        try {
            connection = this.connectionFactory.getConnection();
            final Response<SearchResult> result = searchForServiceById(connection, rs.getId());
            if (result.getResult() != null) {
                final String currentDn = result.getResult().getEntry().getDn();

                connection = this.connectionFactory.getConnection();
                final ModifyOperation operation = new ModifyOperation(connection);

                final List<AttributeModification> mods = new LinkedList<AttributeModification>();

                final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
                for (final LdapAttribute attr : entry.getAttributes()) {
                    mods.add(new AttributeModification(AttributeModificationType.REPLACE, attr));
                }
                final ModifyRequest request = new ModifyRequest(currentDn, mods.toArray(new AttributeModification[] {}));
                operation.execute(request);

                return rs;
            }
        } catch (final LdapException e) {
            log.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        Connection connection = null;
        try {
            connection = this.connectionFactory.getConnection();

            final Response<SearchResult> result = searchForServiceById(connection, registeredService.getId());
            if (result.getResult() != null) {
                final LdapEntry entry = result.getResult().getEntry();

                final DeleteOperation delete = new DeleteOperation(connection);
                final DeleteRequest request = new DeleteRequest(entry.getDn());
                final Response<Void> res = delete.execute(request);
                return res.getResultCode() == ResultCode.SUCCESS;
            }
        } catch (final LdapException e) {
            log.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }

        return false;
    }

    @Override
    public List<RegisteredService> load() {
        Connection connection = null;
        final List<RegisteredService> list = new LinkedList<RegisteredService>();
        try {
            connection = this.connectionFactory.getConnection();
            final SearchFilter filter = new SearchFilter(this.loadFilter);
            final Response<SearchResult> result = executeSearchOperation(connection, filter);
            if (result.getResult() != null) {
                for (final LdapEntry entry : result.getResult().getEntries()) {
                    final RegisteredService svc = this.ldapServiceMapper.mapToRegisteredService(entry);
                    list.add(svc);
                }
            }
        } catch (final LdapException e) {
            log.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }
        return list;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        Connection connection = null;
        try {
            connection = this.connectionFactory.getConnection();

            final Response<SearchResult> result = searchForServiceById(connection, id);
            if (result.getResult() != null) {
                return this.ldapServiceMapper.mapToRegisteredService(result.getResult().getEntry());
            }
        } catch (final LdapException e) {
            log.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }

        return null;
    }

    private Response<SearchResult> searchForServiceById(final Connection connection, final long id) throws LdapException {
        final SearchFilter filter = new SearchFilter(this.searchFilter);
        filter.setParameter(0, id);

        return executeSearchOperation(connection, filter);
    }

    private Response<SearchResult> executeSearchOperation(final Connection connection, final SearchFilter filter) throws LdapException {
        final SearchOperation searchOperation = new SearchOperation(connection, this.cacheStrategy);

        final SearchRequest searchRequest = new SearchRequest(this.baseDn, filter, this.attributesToReturn);
        searchRequest.setReturnAttributes(this.attributesToReturn);
        searchRequest.setSearchEntryHandlers(this.searchEntryHandlers);
        searchRequest.setDerefAliases(this.derefAliases);
        searchRequest.setFollowReferrals(this.followReferrals);
        searchRequest.setSearchScope(this.searchScope);
        searchRequest.setSizeLimit(this.sizeLimit);
        searchRequest.setSortBehavior(this.sortBehavior);
        searchRequest.setTypesOnly(this.typesOnly);
        searchRequest.setTimeLimit(this.timeLimit);

        log.debug("Using search request [{}]", searchRequest.toString());
        return searchOperation.execute(searchRequest);
    }

    public void setConnectionFactory(@NotNull final ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    public void setCacheStrategy(@NotNull final Cache<SearchRequest> cache) {
        this.cacheStrategy = cache;
    }

    public void setLdapServiceMapper(final LdapRegisteredServiceMapper ldapServiceMapper) {
        this.ldapServiceMapper = ldapServiceMapper;
    }

    public final void setBaseDn(@NotNull final String baseDn) {
        this.baseDn = baseDn;
    }

    public final void setSizeLimit(final int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public final void setDerefAliases(final DerefAliases derefAliases) {
        this.derefAliases = derefAliases;
    }

    public final void setFollowReferrals(final boolean followReferrals) {
        this.followReferrals = followReferrals;
    }

    public final void setTypesOnly(final boolean typesOnly) {
        this.typesOnly = typesOnly;
    }

    public final void setSearchScope(final SearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public final void setSortBehavior(final SortBehavior sortBehavior) {
        this.sortBehavior = sortBehavior;
    }

    public final void setSearchEntryHandlers(final SearchEntryHandler[] searchEntryHandlers) {
        this.searchEntryHandlers = searchEntryHandlers;
    }

    public void setSearchFilter(@NotNull final String filter) {
        this.searchFilter = filter;
    }

    public void setLoadFilter(@NotNull final String filter) {
        this.loadFilter = filter;
    }

    public void setAttributesToReturn(final String[] attrs) {
        this.attributesToReturn = attrs;
    }

    public void setTimeLimit(final int limit) {
        this.timeLimit = limit;
    }
}
