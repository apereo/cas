package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.util.RegexUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ServicesManager} interface that organizes services by domain into
 * a hash for quicker lookup.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class DomainServicesManager implements ServicesManager, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainServicesManager.class);
    private static final long serialVersionUID = -8581398063126547772L;

    private final ServiceRegistryDao serviceRegistryDao;

    @Autowired
    private transient ApplicationEventPublisher eventPublisher;

    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();
    private Map<String, TreeSet<RegisteredService>> domains = new ConcurrentHashMap<>();

    /**
     * This regular expression is used to strip the domain form the serviceId that is set in
     * the Service and also passed as the service parameter to the login endpoint.
     */
    private final Pattern domainPattern = RegexUtils.createPattern("^(https?|imaps?)://([^:/]+)/i");

    /**
     * Instantiates a new default services manager impl.
     *
     * @param serviceRegistryDao the service registry dao
     */
    public DomainServicesManager(final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    @Audit(action = "DELETE_SERVICE",
            actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService service = findServiceBy(id);
        if (service != null) {
            this.serviceRegistryDao.delete(service);
            this.services.remove(id);
            this.domains.get(getDomain(service.getServiceId())).remove(service);
            publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        }
        return service;
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        return service != null ? findServiceBy(service.getId()) : null;
    }


    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> predicate) {
        return services.values().stream()
                .filter(predicate)
                .sorted()
                .collect(Collectors.toSet());
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final Service serviceId, final Class<T> clazz) {
        return findServiceBy(serviceId.getId(), clazz);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(final String serviceId, final Class<T> clazz) {
        return getServicesForDomain(getDomain(serviceId)).stream()
                .filter(s -> s.getClass().isAssignableFrom(clazz) && s.matches(serviceId))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);

        try {
            return r == null ? null : r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        String domain = serviceId != null ? getDomain(serviceId) : StringUtils.EMPTY;
        LOGGER.debug("Domain mapped to the service identifier is [{}]", serviceId);

        domain = domains.containsKey(domain) ? domain : "default";
        LOGGER.debug("Looking up services under domain [{}] for service identifier [{}]", domain, serviceId);

        final Set<RegisteredService> registeredServices = domains.get(domain);
        if (registeredServices == null || registeredServices.isEmpty()) {
            LOGGER.debug("No services could be located for domain [{}]", domain);
            return null;
        }
        return registeredServices
                .stream()
                .filter(s -> s.matches(serviceId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return services.values()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return matchesExistingService(service.getId());
    }

    @Override
    public boolean matchesExistingService(final String service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "SAVE_SERVICE",
            actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService, final boolean publishEvent) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        addToDomain(r, this.domains);
        if (publishEvent) {
            publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        }
        return r;
    }

    /**
     * Load services that are provided by the DAO.
     */
    @Scheduled(initialDelayString = "${cas.serviceRegistry.schedule.startDelay:20000}",
            fixedDelayString = "${cas.serviceRegistry.schedule.repeatInterval:60000}")
    @Override
    @PostConstruct
    public void load() {
        LOGGER.debug("Loading services from [{}]", this.serviceRegistryDao);
        this.services = this.serviceRegistryDao.load().stream()
                .collect(Collectors.toConcurrentMap(r -> {
                    LOGGER.debug("Adding registered service [{}]", r.getServiceId());
                    return r.getId();
                }, r -> r, (r, s) -> s == null ? r : s));
        final Map<String, TreeSet<RegisteredService>> localDomains = new ConcurrentHashMap<>();
        this.services.values().stream().forEach(r -> addToDomain(r, localDomains));
        this.domains = localDomains;
        publishEvent(new CasRegisteredServicesLoadedEvent(this, services.values()));
        LOGGER.info("Loaded [{}] services from [{}].", this.services.size(), this.serviceRegistryDao);
    }


    @Override
    public int count() {
        return services.size();
    }

    @Override
    public List<String> getDomains() {
        return domains.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(final String domain) {
        return domains.containsKey(domain) ? domains.get(domain) : Collections.EMPTY_LIST;
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }

    private String getDomain(final String service) {
        final Matcher match = domainPattern.matcher(service.toLowerCase());
        final String domain = match.lookingAt() && !match.group(1).contains("*") ? match.group(1) : "default";
        LOGGER.debug("Domain found for service identifier [{}]", domain);
        return domain;
    }

    private void addToDomain(final RegisteredService r, final Map<String, TreeSet<RegisteredService>> map) {
        final String domain = getDomain(r.getServiceId());
        final TreeSet<RegisteredService> services;
        if (map.containsKey(domain)) {
            services = map.get(domain);
        } else {
            services = new TreeSet<>();
        }
        LOGGER.debug("Added service [{}] mapped to domain definition [{}]", r, domain);
        services.add(r);
        map.put(domain, services);
    }
}
