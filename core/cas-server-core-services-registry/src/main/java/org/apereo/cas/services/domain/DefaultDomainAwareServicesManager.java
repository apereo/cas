package org.apereo.cas.services.domain;

import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.DomainAwareServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
public class DefaultDomainAwareServicesManager extends AbstractServicesManager implements DomainAwareServicesManager {
    private final Map<String, TreeSet<RegisteredService>> domains = new ConcurrentHashMap<>();

    private final RegisteredServiceDomainExtractor registeredServiceDomainExtractor;

    public DefaultDomainAwareServicesManager(final ServiceRegistry serviceRegistry,
                                             final ApplicationEventPublisher eventPublisher,
                                             final RegisteredServiceDomainExtractor registeredServiceDomainExtractor,
                                             final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
        this.registeredServiceDomainExtractor = registeredServiceDomainExtractor;
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
    protected Stream<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        val mappedDomain = StringUtils.isNotBlank(serviceId) ? registeredServiceDomainExtractor.extract(serviceId) : StringUtils.EMPTY;
        LOGGER.trace("Domain mapped to the service identifier is [{}]", mappedDomain);

        val domain = domains.containsKey(mappedDomain) ? mappedDomain : RegisteredServiceDomainExtractor.DOMAIN_DEFAULT;
        LOGGER.trace("Looking up services under domain [{}] for service identifier [{}]", domain, serviceId);

        val registeredServices = getServicesForDomain(domain);
        if (registeredServices == null || registeredServices.isEmpty()) {
            LOGGER.debug("No services could be located for domain [{}]", domain);
            return Stream.empty();
        }
        return registeredServices.stream();
    }

    @Override
    protected void saveInternal(final RegisteredService service) {
        addToDomain(service, this.domains);
    }

    @Override
    protected void loadInternal() {
        val localDomains = new ConcurrentHashMap<String, TreeSet<RegisteredService>>();
        getAllServices().forEach(r -> addToDomain(r, localDomains));
        this.domains.clear();
        this.domains.putAll(localDomains);
    }

    @Override
    public Stream<String> getDomains() {
        return this.domains.keySet().stream().sorted();
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return this.domains.containsKey(domain) ? this.domains.get(domain) : new ArrayList<>(0);
    }

    private void addToDomain(final RegisteredService r, final Map<String, TreeSet<RegisteredService>> map) {
        val domain = registeredServiceDomainExtractor.extract(r.getServiceId());
        val services = map.containsKey(domain)
            ? map.get(domain)
            : new TreeSet<RegisteredService>();
        LOGGER.debug("Added service [{}] mapped to domain definition [{}]", r, domain);
        services.add(r);
        map.put(domain, services);
    }
}
