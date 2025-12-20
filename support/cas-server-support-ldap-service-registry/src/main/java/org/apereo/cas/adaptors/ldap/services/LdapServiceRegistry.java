package org.apereo.cas.adaptors.ldap.services;

import module java.base;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceDefinition;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Implementation of the ServiceRegistry interface which stores the services in a LDAP Directory.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@ToString
public class LdapServiceRegistry extends AbstractServiceRegistry implements DisposableBean {

    private final LdapConnectionFactory connectionFactory;

    private final LdapRegisteredServiceMapper ldapServiceMapper;

    private final LdapServiceRegistryProperties ldapProperties;

    public LdapServiceRegistry(final ConnectionFactory connectionFactory,
                               final LdapRegisteredServiceMapper ldapServiceMapper,
                               final LdapServiceRegistryProperties ldapProperties,
                               final ConfigurableApplicationContext applicationContext,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.connectionFactory = new LdapConnectionFactory(connectionFactory);
        this.ldapProperties = ldapProperties;
        this.ldapServiceMapper = Objects.requireNonNullElseGet(ldapServiceMapper,
            () -> new DefaultLdapRegisteredServiceMapper(ldapProperties, new RegisteredServiceJsonSerializer(applicationContext)));
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        invokeServiceRegistryListenerPreSave(rs);
        if (rs.getId() != RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE) {
            return update(rs);
        }
        insert(rs);
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val response = searchForServiceById(registeredService.getId());
        if (LdapUtils.containsResultEntry(response)) {
            val entry = response.getEntry();
            return connectionFactory.executeDeleteOperation(entry);
        }
        LOGGER.debug("Could not locate registered service by id [{}] to delete", registeredService.getId());
        return false;
    }

    @Override
    public void deleteAll() {
        val response = getSearchResultResponse();
        if (LdapUtils.containsResultEntry(response)) {
            response.getEntries().forEach(connectionFactory::executeDeleteOperation);
        }
    }

    @Override
    public Collection<RegisteredService> load() {
        val list = new ArrayList<RegisteredService>();
        val response = getSearchResultResponse();
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (LdapUtils.containsResultEntry(response)) {
            response.getEntries()
                .stream()
                .map(this.ldapServiceMapper::mapToRegisteredService)
                .filter(Objects::nonNull)
                .map(this::invokeServiceRegistryListenerPostLoad)
                .filter(Objects::nonNull)
                .forEach(registeredService -> {
                    publishEvent(new CasRegisteredServiceLoadedEvent(this, registeredService, clientInfo));
                    list.add(registeredService);
                });
        }
        return list;
    }

    @Override
    public @Nullable RegisteredService findServiceById(final long id) {
        val response = searchForServiceById(id);
        if (LdapUtils.containsResultEntry(response)) {
            return this.ldapServiceMapper.mapToRegisteredService(response.getEntry());
        }
        return null;
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
        val response = getSearchResultResponse();
        if (LdapUtils.containsResultEntry(response)) {
            return response.getEntries()
                .stream()
                .map(this.ldapServiceMapper::mapToRegisteredService)
                .filter(Objects::nonNull)
                .count();
        }
        return 0;
    }

    @Override
    public void destroy() {
        connectionFactory.close();
    }

    private RegisteredService insert(final RegisteredService rs) {
        rs.assignIdIfNecessary();
        val entry = this.ldapServiceMapper.mapFromRegisteredService(ldapProperties.getBaseDn(), rs);
        connectionFactory.executeAddOperation(Objects.requireNonNull(entry));
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
            val entry = this.ldapServiceMapper.mapFromRegisteredService(ldapProperties.getBaseDn(), rs);
            connectionFactory.executeModifyOperation(currentDn, Objects.requireNonNull(entry));
        } else {
            LOGGER.debug("Cannot locate DN for registered service with id [{}]. Attempting to save the service", rs.getId());
            insert(rs);
        }
        return rs;
    }

    private SearchResponse getSearchResultResponse() {
        return FunctionUtils.doUnchecked(() -> {
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getLoadFilter());
            return connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(), filter, ldapProperties.getPageSize());
        });
    }

    private SearchResponse searchForServiceById(final Long id) {
        return FunctionUtils.doUnchecked(() -> {
            val filter = LdapUtils.newLdaptiveSearchFilter(ldapProperties.getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME, CollectionUtils.wrap(id.toString()));
            return connectionFactory.executeSearchOperation(ldapProperties.getBaseDn(),
                filter, ldapProperties.getPageSize());
        });
    }

    private @Nullable String getCurrentDnForRegisteredService(final RegisteredService rs) {
        val response = searchForServiceById(rs.getId());
        if (LdapUtils.containsResultEntry(response)) {
            return response.getEntry().getDn();
        }
        return null;
    }
}
