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
package org.jasig.cas.adaptors.ldap.services;

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
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the ServiceRegistryDao interface which stores the services in a LDAP Directory.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public final class LdapServiceRegistryDao implements ServiceRegistryDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private ConnectionFactory connectionFactory;

    @NotNull
    private LdapRegisteredServiceMapper ldapServiceMapper = new DefaultLdapRegisteredServiceMapper();

    @NotNull
    private String searchFilter;

    @NotNull
    private String loadFilter;

    @NotNull
    private SearchRequest searchRequest;


    /**
     * Inits the dao with the search filter and load filters.
     */
    @PostConstruct
    public void init() {
        this.searchFilter = '(' + this.ldapServiceMapper.getIdAttribute() +  "={0})";
        this.loadFilter = "(objectClass=" + this.ldapServiceMapper.getObjectClass() + ')';
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        if (rs.getId() != RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            return update(rs);
        }

        Connection connection = null;
        try {
            connection = getConnection();
            final AddOperation operation = new AddOperation(connection);

            final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.searchRequest.getBaseDn(), rs);
            operation.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }
        return rs;
    }

    /**
     * Update the ldap entry with the given registered service.
     *
     * @param rs the rs
     * @return the registered service
     */
    private RegisteredService update(final RegisteredService rs) {
        Connection searchConnection = null;
        try {
            searchConnection = getConnection();
            final Response<SearchResult> response = searchForServiceById(searchConnection, rs.getId());
            if (hasResults(response)) {
                final String currentDn = response.getResult().getEntry().getDn();

                Connection modifyConnection = null;
                try {
                    modifyConnection = getConnection();
                    final ModifyOperation operation = new ModifyOperation(searchConnection);

                    final List<AttributeModification> mods = new ArrayList<>();

                    final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.searchRequest.getBaseDn(), rs);
                    for (final LdapAttribute attr : entry.getAttributes()) {
                        if (!attr.getName().equals(this.ldapServiceMapper.getIdAttribute())) {
                            mods.add(new AttributeModification(AttributeModificationType.REPLACE, attr));
                        }
                    }
                    final ModifyRequest request = new ModifyRequest(currentDn, mods.toArray(new AttributeModification[]{}));
                    operation.execute(request);
                }catch (final LdapException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    LdapUtils.closeConnection(modifyConnection);
                }
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(searchConnection);
        }
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        Connection connection = null;
        try {
            connection = getConnection();

            final Response<SearchResult> response = searchForServiceById(connection, registeredService.getId());
            if (hasResults(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final DeleteOperation delete = new DeleteOperation(connection);
                final DeleteRequest request = new DeleteRequest(entry.getDn());
                final Response<Void> res = delete.execute(request);
                return res.getResultCode() == ResultCode.SUCCESS;
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }

        return false;
    }

    @Override
    public List<RegisteredService> load() {
        Connection connection = null;
        final List<RegisteredService> list = new LinkedList<>();
        try {
            connection = getConnection();
            final Response<SearchResult> response =
                    executeSearchOperation(connection, new SearchFilter(this.loadFilter));
            if (hasResults(response)) {
                for (final LdapEntry entry : response.getResult().getEntries()) {
                    final RegisteredService svc = this.ldapServiceMapper.mapToRegisteredService(entry);
                    list.add(svc);
                }
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }
        return list;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        Connection connection = null;
        try {
            connection = getConnection();

            final Response<SearchResult> response = searchForServiceById(connection, id);
            if (hasResults(response)) {
                return this.ldapServiceMapper.mapToRegisteredService(response.getResult().getEntry());
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
        } finally {
            LdapUtils.closeConnection(connection);
        }

        return null;
    }

    /**
     * Search for service by id.
     *
     * @param connection the connection
     * @param id the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForServiceById(final Connection connection, final long id)
            throws LdapException {

        final SearchFilter filter = new SearchFilter(this.searchFilter);
        filter.setParameter(0, id);
        return executeSearchOperation(connection, filter);
    }

    /**
     * Execute search operation.
     *
     * @param connection the connection
     * @param filter the filter
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> executeSearchOperation(final Connection connection, final SearchFilter filter)
            throws LdapException {

        final SearchOperation searchOperation = new SearchOperation(connection);
        final SearchRequest request = newRequest(filter);
        logger.debug("Using search request {}", request.toString());
        return searchOperation.execute(request);
    }

    public void setConnectionFactory(@NotNull final ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    public void setLdapServiceMapper(final LdapRegisteredServiceMapper ldapServiceMapper) {
        this.ldapServiceMapper = ldapServiceMapper;
    }

    public void setSearchRequest(@NotNull final SearchRequest request) {
        this.searchRequest = request;
    }

    /**
     * Checks to see if response has a result.
     *
     * @param response the response
     * @return true, if successful
     */
    private boolean hasResults(final Response<SearchResult> response) {
        final SearchResult result = response.getResult();
        if (result != null && result.getEntry() != null) {
            return true;
        }

        logger.trace("Requested ldap operation did not return a result or an ldap entry. Code: {}, Message: {}",
                response.getResultCode(), response.getMessage());
        return false;
    }

    /**
     * Builds a new request.
     *
     * @param filter the filter
     * @return the search request
     */
    private SearchRequest newRequest(final SearchFilter filter) {

        final SearchRequest sr = new SearchRequest(this.searchRequest.getBaseDn(), filter);
        sr.setBinaryAttributes(ReturnAttributes.ALL_USER.value());
        sr.setDerefAliases(this.searchRequest.getDerefAliases());
        sr.setSearchEntryHandlers(this.searchRequest.getSearchEntryHandlers());
        sr.setSearchReferenceHandlers(this.searchRequest.getSearchReferenceHandlers());
        sr.setReferralHandler(this.searchRequest.getReferralHandler());
        sr.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        sr.setSearchScope(this.searchRequest.getSearchScope());
        sr.setSizeLimit(this.searchRequest.getSizeLimit());
        sr.setSortBehavior(this.searchRequest.getSortBehavior());
        sr.setTimeLimit(this.searchRequest.getTimeLimit());
        sr.setTypesOnly(this.searchRequest.getTypesOnly());
        sr.setControls(this.searchRequest.getControls());
        return sr;
    }

    /**
     * Gets connection from the factory.
     * Opens the connection if needed.
     *
     * @return the connection
     * @throws LdapException the ldap exception
     */
    private Connection getConnection() throws LdapException {
        final Connection c = this.connectionFactory.getConnection();
        if (!c.isOpen()) {
            c.open();
        }
        return c;
    }
}
