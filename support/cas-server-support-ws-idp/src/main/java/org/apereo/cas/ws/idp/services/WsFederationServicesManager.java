package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.authentication.principal.WsFederationWebApplicationService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of the {@link ServicesManager} interface that loads WsFederation services
 * into a hash to be looked up by entityId.
 *
 * @author Travis Schmidt
 * @since 6.1
 */
@Slf4j
public class WsFederationServicesManager extends AbstractServicesManager {

    private Set<RegisteredService> orderedServices = new ConcurrentSkipListSet<>();

    private Map<String, RegisteredService> mappedServiecs = new ConcurrentHashMap<String, RegisteredService>();

    public WsFederationServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher, final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    @Override
    protected Collection<RegisteredService> getCandidateServicesToMatch(final String serviceId) {
        return this.mappedServiecs.values();
    }

    @Override
    protected void deleteInternal(final RegisteredService service) {
        this.orderedServices.remove(service);
    }

    @Override
    protected void saveInternal(final RegisteredService service) {
        val samlService = (WSFederationRegisteredService) service;
        this.mappedServiecs.put(samlService.getServiceId(), service);
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        return findServiceBy(service.getId());
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        return this.mappedServiecs.get(serviceId);
    }

    @Override
    protected void loadInternal() {
        this.mappedServiecs = new ConcurrentHashMap<>();
        val services = getAllServices().stream().filter(r -> r instanceof WSFederationRegisteredService).collect(toList());
        services.forEach(s -> this.mappedServiecs.put(((WSFederationRegisteredService) s).getServiceId(), s));
        LOGGER.trace("WsFederation Services - [{}] loaded", services.size());
    }

    @Override
    protected boolean filterInternal(final RegisteredService service) {
        return supports(service);
    }

    @Override
    public boolean supports(final Service service) {
        return service != null && service instanceof WsFederationWebApplicationService;
    }

    @Override
    public boolean supports(final RegisteredService service) {
        return service != null && service instanceof WSFederationRegisteredService;
    }

    @Override
    public boolean supports(final Class clazz) {
        return clazz != null && clazz.isAssignableFrom(WSFederationRegisteredService.class);
    }
}
