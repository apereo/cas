package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;

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

    /**
     * Reports, per SAML service, whether a metadata resolver can serve it. Each resolver is asked about a metadata
     * location once per health check and the answer is shared by every service with that location: a metadata store
     * such as {@code jdbc://}, a metadata query server or a federation aggregate is one backend, however many service
     * definitions point at it, and probing it once per service turned every health check into as many calls.
     * Each service also carries what its resolvers report about their sources, such as a metadata backup file that
     * its source has not confirmed for longer than allowed, which flags the service as {@code metadataStale}.
     *
     * @param builder the health builder
     */
    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val samlServices = servicesManager.findServiceBy(SamlRegisteredService.class::isInstance);
        val availableResolvers = metadataResolutionPlan.getRegisteredMetadataResolvers();
        LOGGER.trace("There are [{}] metadata resolver(s) available in the chain", availableResolvers.size());

        builder.up();
        builder.withDetail("name", getClass().getSimpleName());
        var count = new AtomicInteger();
        val probes = new HashMap<Pair<SamlRegisteredServiceMetadataResolver, String>, Boolean>();
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
                    .anyMatch(r -> probes.computeIfAbsent(Pair.of(r, StringUtils.defaultString(service.getMetadataLocation())),
                        _ -> r.isAvailable(service)));
                map.put("availability", BooleanUtils.toStringYesNo(availability));
                availableResolvers
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(r -> r.supports(service))
                    .map(r -> r.describe(service, new CriteriaSet()))
                    .forEach(map::putAll);
                if (Boolean.TRUE.equals(map.get("metadataBackupStale"))) {
                    map.put("metadataStale", Boolean.TRUE);
                }
                builder.withDetail(service.getName(), map);
                if (!availability) {
                    LOGGER.debug("No metadata resolver is available for service [{}]", service.getName());
                    count.getAndIncrement();
                }
            });
        if (!samlServices.isEmpty() && count.intValue() == samlServices.size()) {
            builder.down();
        }
    }
}
