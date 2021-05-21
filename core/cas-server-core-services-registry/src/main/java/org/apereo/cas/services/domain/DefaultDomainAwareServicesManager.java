package org.apereo.cas.services.domain;

import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Implementation of the {@link ServicesManager} interface that organizes services by domain into
 * a hash for quicker lookup.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
@Slf4j
public class DefaultDomainAwareServicesManager extends AbstractServicesManager {
    private final Map<String, TreeSet<RegisteredService>> domains = new ConcurrentHashMap<>();

    private final RegisteredServiceDomainExtractor registeredServiceDomainExtractor;

    public DefaultDomainAwareServicesManager(final ServicesManagerConfigurationContext context,
                                             final RegisteredServiceDomainExtractor registeredServiceDomainExtractor) {
        super(context);
        this.registeredServiceDomainExtractor = registeredServiceDomainExtractor;
    }

    @Override
    public Stream<String> getDomains() {
        return this.domains.keySet().stream().sorted();
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return this.domains.containsKey(domain) ? this.domains.get(domain) : new ArrayList<>(0);
    }

    @Override
    protected void deleteInternal(final RegisteredService service) {
        val domain = registeredServiceDomainExtractor.extract(service.getServiceId());
        val entries = this.domains.get(domain);
        entries.remove(service);
        if (entries.isEmpty()) {
            this.domains.remove(domain);
        }
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        val mappedDomain = StringUtils.isNotBlank(serviceId) ? registeredServiceDomainExtractor.extract(serviceId) : StringUtils.EMPTY;
        LOGGER.trace("Domain mapped to the service identifier is [{}]", mappedDomain);

        val domain = domains.containsKey(mappedDomain) ? mappedDomain : RegisteredServiceDomainExtractor.DOMAIN_DEFAULT;
        LOGGER.trace("Looking up services under domain [{}] for service identifier [{}]", domain, serviceId);

        val registeredServices = getServicesForDomain(domain);
        if (registeredServices == null || registeredServices.isEmpty()) {
            LOGGER.debug("No services could be located for domain [{}]", domain);
            return new ArrayList<>(0);
        }
        return registeredServices;
    }

    @Override
    protected void saveInternal(final RegisteredService service) {
        this.domains
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().stream().anyMatch(s -> s.getId() == service.getId()))
            .map(Map.Entry::getKey)
            .findFirst()
            .ifPresent(key -> {
                val servicesForDomain = domains.get(key);
                servicesForDomain.removeIf(s -> s.getId() == service.getId());
                if (servicesForDomain.isEmpty()) {
                    domains.remove(key);
                }
            });
        addToDomain(service);
    }

    @Override
    protected void loadInternal(final RegisteredService service) {
        addToDomain(service);
    }

    private void addToDomain(final RegisteredService service) {
        val domain = registeredServiceDomainExtractor.extract(service.getServiceId());
        val services = domains.containsKey(domain)
            ? domains.get(domain)
            : new TreeSet<RegisteredService>();
        LOGGER.debug("Added service [{}] mapped to domain definition [{}]", service, domain);
        services.removeIf(s -> s.getId() == service.getId());
        services.add(service);
        domains.put(domain, services);
    }
}
