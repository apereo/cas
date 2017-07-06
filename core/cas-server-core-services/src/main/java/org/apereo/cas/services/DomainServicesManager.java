package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link ServicesManager} interface.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class DomainServicesManager implements ServicesManager, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServicesManager.class);
    private static final long serialVersionUID = -8581398063126547772L;

    private final ServiceRegistryDao serviceRegistryDao;

    @Autowired
    private transient ApplicationEventPublisher eventPublisher;

    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();
    private Map<String, TreeSet<RegisteredService>> domains = new ConcurrentHashMap<>();

    Pattern domainPattern = Pattern.compile("^https?://([^:/]+)");

    /**
     * Instantiates a new default services manager impl.
     *
     * @param serviceRegistryDao the service registry dao
     */
    public DomainServicesManager(final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
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
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);

        try {
            return r == null ? null : r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(services.values().stream().sorted().collect(Collectors.toList()));
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        addToDomain(r,this.domains);
        publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        return r;
    }

    @Override
    public List<String> getDomains() {
        return domains.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Collection<RegisteredService> getServicesForDomain(String domain) {
        if(domain.contains("/")) {
            domain = getDomain(domain);
        }
        return domains.containsKey(domain) ? domains.get(domain) : Collections.EMPTY_LIST;
    }


    /**
     * Load services that are provided by the DAO.
     */
    @Scheduled(initialDelayString = "${cas.serviceRegistry.startDelay:20000}",
            fixedDelayString = "${cas.serviceRegistry.repeatInterval:60000}")
    @Override
    @PostConstruct
    public void load() {
        LOGGER.debug("Loading services from [{}]", this.serviceRegistryDao);
        this.services = this.serviceRegistryDao.load().stream()
                .collect(Collectors.toConcurrentMap(r -> {
                    LOGGER.debug("Adding registered service [{}]", r.getServiceId());
                    return r.getId();
                }, r -> r, (r, s) -> s == null ? r : s));
        Map<String, TreeSet<RegisteredService>> localDomains = new ConcurrentHashMap<>();
        this.services.values().stream().forEach(r -> addToDomain(r,localDomains));
        this.domains = localDomains;
        LOGGER.info("Loaded [{}] services from [{}].", this.services.size(), this.serviceRegistryDao);
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        String domain = serviceId != null ? getDomain(serviceId) : "";
        domain = domains.containsKey(domain) ? domain : "default";
        return domains.get(domain)
                .stream()
                .filter(s -> s.matches(serviceId))
                .findFirst().orElse(null);
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(Service serviceId, Class<T> clazz) {
        return (T)findServiceBy(serviceId.getId());
    }

    @Override
    public <T extends RegisteredService> T findServiceBy(String serviceId, Class<T> clazz) {
        return (T)findServiceBy(serviceId);
    }

    @Override
    public boolean matchesExistingService(final String service) {
        return findServiceBy(service) != null;
    }

    @Override
    public int count() {
        return services.size();
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }

    private String getDomain(String service) {
        Matcher match = domainPattern.matcher(service.toLowerCase());
        return match.lookingAt() && !match.group(1).contains("*") ? match.group(1) : "default";
    }

    private void addToDomain(RegisteredService r, Map<String,TreeSet<RegisteredService>> map) {
        String domain = getDomain(r.getServiceId());
        TreeSet<RegisteredService> services;
        if (map.containsKey(domain))
            services = map.get(domain);
        else
            services = new TreeSet<RegisteredService>();
        services.add(r);
        map.put(domain,services);
    }
}
