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

import java.util.HashMap;
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

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        val samlServices = servicesManager.findServiceBy(registeredService -> registeredService instanceof SamlRegisteredService);
        val availableResolvers = this.metadataResolutionPlan.getRegisteredMetadataResolvers();
        LOGGER.debug("There are [{}] metadata resolver(s) available in the chain", availableResolvers.size());

        builder.up();

        samlServices.stream()
            .map(SamlRegisteredService.class::cast)
            .forEach(service -> {
                availableResolvers
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(r -> {
                        LOGGER.debug("Evaluating whether metadata resolver [{}] is available for service [{}]", r.getName(), service.getName());
                        val available = r.isAvailable(service);
                        val map = new HashMap<String, Object>();
                        map.put("name", service.getName());
                        map.put("id", service.getId());
                        map.put("metadataLocation", service.getMetadataLocation());
                        map.put("serviceId", service.getServiceId());
                        map.put("availability", BooleanUtils.toStringYesNo(available));
                        builder.withDetail(service.getName(), map);
                        if (!available) {
                            builder.down();
                        }
                    });
            });
    }
}
