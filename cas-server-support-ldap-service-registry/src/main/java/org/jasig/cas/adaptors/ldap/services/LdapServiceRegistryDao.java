package org.jasig.cas.adaptors.ldap.services;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
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
@Component("ldapServiceRegistryDao")
public final class LdapServiceRegistryDao implements ServiceRegistryDao {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapServiceRegistryConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapServiceRegistryMapper")
    private LdapRegisteredServiceMapper ldapServiceMapper = new DefaultLdapRegisteredServiceMapper();

    @NotNull
    private String searchFilter;

    @NotNull
    private String loadFilter;

    @Nullable
    @Autowired(required=false)
    @Qualifier("ldapServiceRegistrySearchRequest")
    private SearchRequest searchRequest;

    /**
     * Inits the dao with the search filter and load filters.
     */
    @PostConstruct
    public void init() {
        this.searchFilter = '(' + this.ldapServiceMapper.getIdAttribute() +  "={0})";
        logger.debug("Configured search filter to {}", this.searchFilter);
        this.loadFilter = "(objectClass=" + this.ldapServiceMapper.getObjectClass() + ')';
        logger.debug("Configured load filter to {}", this.loadFilter);
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {

        if (this.ldapServiceMapper != null && this.searchRequest != null) {
            if (rs.getId() != RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                return update(rs);
            }

            try (final Connection connection = getConnection()) {
                final AddOperation operation = new AddOperation(connection);

                final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.searchRequest.getBaseDn(), rs);
                operation.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            } catch (final LdapException e) {
                logger.error(e.getMessage(), e);
            }
            return rs;
        }
        return null;
    }

    /**
     * Update the ldap entry with the given registered service.
     *
     * @param rs the rs
     * @return the registered service
     */
    private RegisteredService update(final RegisteredService rs) {
        String currentDn = null;
        if (ldapServiceMapper == null || searchRequest == null) {
            return null;
        }

        try (final Connection searchConnection = getConnection()) {
            final Response<SearchResult> response = searchForServiceById(searchConnection, rs.getId());
            if (hasResults(response)) {
                currentDn = response.getResult().getEntry().getDn();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (StringUtils.isNotBlank(currentDn)) {
            logger.debug("Updating registered service at {}", currentDn);

            try (final Connection modifyConnection = getConnection()) {
                final ModifyOperation operation = new ModifyOperation(modifyConnection);
                final List<AttributeModification> mods = new ArrayList<>();

                final LdapEntry entry = this.ldapServiceMapper.mapFromRegisteredService(this.searchRequest.getBaseDn(), rs);
                for (final LdapAttribute attr : entry.getAttributes()) {
                    if (!attr.getName().equals(this.ldapServiceMapper.getIdAttribute())) {
                        mods.add(new AttributeModification(AttributeModificationType.REPLACE, attr));
                    }
                }
                final ModifyRequest request = new ModifyRequest(currentDn, mods.toArray(new AttributeModification[]{}));
                operation.execute(request);
            } catch (final LdapException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {

        try (final Connection connection = getConnection()) {
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
        }
        return false;
    }

    @Override
    public List<RegisteredService> load() {
        final List<RegisteredService> list = new LinkedList<>();
        if (ldapServiceMapper == null) {
            return list;
        }

        try (final Connection connection = getConnection()) {
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
        }
        return list;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        if (ldapServiceMapper == null) {
            return null;
        }

        try (final Connection connection = getConnection()) {
            final Response<SearchResult> response = searchForServiceById(connection, id);
            if (hasResults(response)) {
                return this.ldapServiceMapper.mapToRegisteredService(response.getResult().getEntry());
            }
        } catch (final LdapException e) {
            logger.error(e.getMessage(), e);
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

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
