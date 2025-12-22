package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.query.BasicRegisteredServiceQueryIndex;
import org.apereo.cas.services.query.RegisteredServiceQueryAttribute;
import org.apereo.cas.services.query.RegisteredServiceQueryIndex;
import org.apereo.cas.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link BaseServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public abstract class BaseServicesManagerRegisteredServiceLocator implements ServicesManagerRegisteredServiceLocator {
    
    private int order = DEFAULT_ORDER;

    private BiPredicate<RegisteredService, Service> registeredServiceFilter = (registeredService, service) -> {
        val supportedType = supports(registeredService, service);
        return supportedType && registeredService.matches(service.getId());
    };

    @Override
    public @Nullable RegisteredService locate(final Collection<? extends RegisteredService> candidates, final Service service) {
        return candidates
            .stream()
            .filter(registeredService -> supports(registeredService, service))
            .filter(registeredService -> registeredServiceFilter.test(registeredService, service))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        val serviceType = getRegisteredServiceIndexedType();
        return serviceType.getValue().isAssignableFrom(registeredService.getClass())
                && registeredService.getFriendlyName().equalsIgnoreCase(serviceType.getKey());
    }

    @Override
    public List<RegisteredServiceQueryIndex> getRegisteredServiceIndexes() {
        val registeredServiceIndexedType = getRegisteredServiceIndexedType().getValue();
        return CollectionUtils.wrapArrayList(BasicRegisteredServiceQueryIndex.hashIndex(
                new RegisteredServiceQueryAttribute(registeredServiceIndexedType, long.class, "id")),
            BasicRegisteredServiceQueryIndex.hashIndex(
                new RegisteredServiceQueryAttribute(registeredServiceIndexedType, String.class, "name")),
            BasicRegisteredServiceQueryIndex.hashIndex(
                new RegisteredServiceQueryAttribute(registeredServiceIndexedType, String.class, "serviceId")),
            BasicRegisteredServiceQueryIndex.hashIndex(
                new RegisteredServiceQueryAttribute(registeredServiceIndexedType, String.class, "friendlyName")),
            BasicRegisteredServiceQueryIndex.hashIndex(
                new RegisteredServiceQueryAttribute(registeredServiceIndexedType, String.class, "@class")));
    }
    
    protected abstract Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType();
}
