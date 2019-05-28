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

/**
 * This is {@link SamlRegisteredServiceMetadataHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SamlRegisteredServiceMetadataHealthIndicator extends AbstractHealthIndicator {
    private final SamlRegisteredServiceMetadataResolutionPlan metadataResolutionPlan;
    private final ServicesManager servicesManager;

    /**
     * Check for availability of metadata sources.
     * Only need 1 valid resolver for metadata to be 'available'.
     *
     * @param builder the health builder to report back status
     */
    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val samlServices = servicesManager.findServiceBy(registeredService -> registeredService instanceof SamlRegisteredService);
        val availableResolvers = this.metadataResolutionPlan.getRegisteredMetadataResolvers();
        LOGGER.debug("There are [{}] metadata resolver(s) available in the chain", availableResolvers.size());

        builder.up();
        builder.withDetail("name", getClass().getSimpleName());

        samlServices
            .stream()
            .map(SamlRegisteredService.class::cast)
            .forEach(service -> {
                val map = new LinkedHashMap<String, Object>();
                map.put("name", service.getName());
                map.put("id", service.getId());
                map.put("metadataLocation", service.getMetadataLocation());
                map.put("serviceId", service.getServiceId());
                val available = availableResolvers
                    .stream()
                    .filter(Objects::nonNull)
                    .peek(r -> LOGGER.debug("Checking if metadata resolver [{}] is available for service [{}]", r.getName(), service.getName()))
                    .anyMatch(r -> r.isAvailable(service));
                map.put("availability", BooleanUtils.toStringYesNo(available));
                builder.withDetail(service.getName(), map);
                if (!available) {
                    LOGGER.debug("No metadata resolver is available for service [{}]", service.getName());
                    builder.down();
                }
            });
    }
}
