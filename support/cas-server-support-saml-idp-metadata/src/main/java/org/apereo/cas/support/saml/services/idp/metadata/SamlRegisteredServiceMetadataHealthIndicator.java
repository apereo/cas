package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link SamlRegisteredServiceMetadataHealthIndicator}.
 * Only need 1 valid resolver for metadata to be 'available'.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SamlRegisteredServiceMetadataHealthIndicator extends AbstractHealthIndicator {
    private final SamlRegisteredServiceMetadataResolutionPlan metadataResolutionPlan;

    private final ServicesManager servicesManager;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val samlServices = servicesManager.findServiceBy(registeredService -> registeredService instanceof SamlRegisteredService);
        val availableResolvers = this.metadataResolutionPlan.getRegisteredMetadataResolvers();
        LOGGER.trace("There are [{}] metadata resolver(s) available in the chain", availableResolvers.size());

        builder.up();
        builder.withDetail("name", getClass().getSimpleName());
        var count = new AtomicInteger();
        samlServices
            .stream()
            .map(SamlRegisteredService.class::cast)
            .forEach(service -> {
                val map = new LinkedHashMap<String, Object>();
                map.put("name", service.getName());
                map.put("id", service.getId());
                map.put("metadataLocation", service.getMetadataLocation());
                map.put("serviceId", service.getServiceId());
                val availability = availableResolvers
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(r -> r.supports(service))
                    .anyMatch(r -> r.isAvailable(service));
                map.put("availability", BooleanUtils.toStringYesNo(availability));
                builder.withDetail(service.getName(), map);
                if (!availability) {
                    LOGGER.debug("No metadata resolver is available for service [{}]", service.getName());
                    count.getAndIncrement();
                }
            });
        if (count.intValue() == samlServices.size()) {
            builder.down();
        }
    }
}
