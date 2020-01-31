package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authentication.principal.OAuthWebApplicationService;

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
 * Implementation of the {@link ServicesManager} interface that handles OAuth services by
 * loading client ids into a hash for lookup.
 *
 * @author Travis Schmidt
 * @since 6.1
 */
@Slf4j
public class Oauth20ServicesManager extends AbstractServicesManager {

    private Set<RegisteredService> orderedServices = new ConcurrentSkipListSet<>();

    private Map<String, RegisteredService> mappedServiecs = new ConcurrentHashMap<String, RegisteredService>();

    public Oauth20ServicesManager(final ServiceRegistry serviceRegistry, final ApplicationEventPublisher eventPublisher, final Set<String> environments) {
        super(serviceRegistry, eventPublisher, environments);
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        return this.mappedServiecs.get(serviceId);
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        return this.mappedServiecs.get(service.getId());
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
        val oauthService = (OAuthRegisteredService) service;
        this.mappedServiecs.put(oauthService.getClientId(), service);
    }



    @Override
    protected void loadInternal() {
        this.mappedServiecs = new ConcurrentHashMap<>();
        val services = getAllServices().stream().filter(r -> r instanceof OAuthRegisteredService).collect(toList());
        services.forEach(s -> this.mappedServiecs.put(((OAuthRegisteredService) s).getClientId(), s));
        LOGGER.trace("OAuth Services - [{}] loaded", services.size());
    }

    @Override
    protected boolean filterInternal(final RegisteredService service) {
        return supports(service);
    }

    @Override
    public boolean supports(final Service service) {
        return service != null && service instanceof OAuthWebApplicationService;
    }

    @Override
    public boolean supports(final RegisteredService service) {
        return service != null && service instanceof OAuthRegisteredService;
    }

    public boolean supports(final Class clazz) {
        return clazz != null && clazz.isAssignableFrom(OAuthRegisteredService.class);
    }
}
