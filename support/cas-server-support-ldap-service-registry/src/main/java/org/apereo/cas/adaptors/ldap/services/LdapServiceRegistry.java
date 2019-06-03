package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchResult;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Implementation of the ServiceRegistry interface which stores the services in a LDAP Directory.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@ToString
public class LdapServiceRegistry extends AbstractServiceRegistry {

    private final ConnectionFactory connectionFactory;

    private final LdapRegisteredServiceMapper ldapServiceMapper;

    private final String baseDn;

    private final String searchFilter;

    private final String loadFilter;

    public LdapServiceRegistry(final ConnectionFactory connectionFactory,
                               final String baseDn,
                               final LdapRegisteredServiceMapper ldapServiceMapper,
                               final LdapServiceRegistryProperties ldapProperties,
                               final ApplicationEventPublisher eventPublisher,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(eventPublisher, serviceRegistryListeners);
        this.connectionFactory = connectionFactory;
        this.baseDn = baseDn;
        if (ldapServiceMapper == null) {
            this.ldapServiceMapper = new DefaultLdapRegisteredServiceMapper(ldapProperties);
        } else {
            this.ldapServiceMapper = ldapServiceMapper;
        }
        this.loadFilter = ldapProperties.getLoadFilter();
        this.searchFilter = ldapProperties.getSearchFilter();
        LOGGER.debug("Configured search filter to [{}] and load filter to [{}]", this.searchFilter, this.loadFilter);
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            invokeServiceRegistryListenerPreSave(rs);
            if (rs.getId() != RegisteredService.INITIAL_IDENTIFIER_VALUE) {
                return update(rs);
            }
            insert(rs);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return rs;
    }

    private RegisteredService insert(final RegisteredService rs) {
        try {
            val entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
            LdapUtils.executeAddOperation(this.connectionFactory, entry);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        val currentDn = getCurrentDnForRegisteredService(rs);

        if (StringUtils.isNotBlank(currentDn)) {
            LOGGER.debug("Updating registered service at [{}]", currentDn);
            val entry = this.ldapServiceMapper.mapFromRegisteredService(this.baseDn, rs);
            LdapUtils.executeModifyOperation(currentDn, this.connectionFactory, entry);
        } else {
            LOGGER.debug("Failed to locate DN for registered service by id [{}]. Attempting to save the service anew", rs.getId());
            insert(rs);
        }
        return rs;
    }

    private String getCurrentDnForRegisteredService(final RegisteredService rs) {
        try {
            val response = searchForServiceById(rs.getId());
            if (LdapUtils.containsResultEntry(response)) {
                return response.getResult().getEntry().getDn();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            val response = searchForServiceById(registeredService.getId());
            if (LdapUtils.containsResultEntry(response)) {
                val entry = response.getResult().getEntry();
                return LdapUtils.executeDeleteOperation(this.connectionFactory, entry);
            }
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * This may be an expensive operation.
     * In order to count the number of available definitions in LDAP,
     * this call will attempt to execute a search query to load services
     * and the results will be counted. Do NOT attempt to call this
     * operation in a loop.
     *
     * @return number of entries in the service registry
     */
    @Override
    public long size() {
        try {
            val response = getSearchResultResponse();
            if (LdapUtils.containsResultEntry(response)) {
                return response.getResult().getEntries()
                    .stream()
                    .map(this.ldapServiceMapper::mapToRegisteredService)
                    .filter(Objects::nonNull)
                    .count();
            }
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Collection<RegisteredService> load() {
        val list = new ArrayList<RegisteredService>();
        try {
            val response = getSearchResultResponse();
            if (LdapUtils.containsResultEntry(response)) {
                response.getResult().getEntries()
                    .stream()
                    .map(this.ldapServiceMapper::mapToRegisteredService)
                    .filter(Objects::nonNull)
                    .map(this::invokeServiceRegistryListenerPostLoad)
                    .filter(Objects::nonNull)
                    .forEach(s -> {
                        publishEvent(new CasRegisteredServiceLoadedEvent(this, s));
                        list.add(s);
                    });
            }
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return list;
    }

    private Response<SearchResult> getSearchResultResponse() throws LdapException {
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, LdapUtils.newLdaptiveSearchFilter(this.loadFilter));
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val response = searchForServiceById(id);
            if (LdapUtils.containsResultEntry(response)) {
                return this.ldapServiceMapper.mapToRegisteredService(response.getResult().getEntry());
            }
        } catch (final LdapException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return load().stream().filter(r -> r.matches(id)).findFirst().orElse(null);
    }

    /**
     * Search for service by id.
     *
     * @param id the id
     * @return the response
     * @throws LdapException the ldap exception
     */
    private Response<SearchResult> searchForServiceById(final Long id) throws LdapException {
        val filter = LdapUtils.newLdaptiveSearchFilter(this.searchFilter,
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(id.toString()));
        return LdapUtils.executeSearchOperation(this.connectionFactory, this.baseDn, filter);
    }
}
