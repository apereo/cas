package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import com.github.benmanes.caffeine.cache.Expiry;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class SamlRegisteredServiceMetadataExpirationPolicy implements Expiry<SamlRegisteredServiceCacheKey, MetadataResolver> {
    private final long defaultExpiration;

    public SamlRegisteredServiceMetadataExpirationPolicy(final Duration metadataCacheExpiration) {
        this.defaultExpiration = metadataCacheExpiration.toNanos();
    }

    @Override
    public long expireAfterCreate(@NonNull final SamlRegisteredServiceCacheKey cacheKey,
                                  final MetadataResolver chainingMetadataResolver,
                                  final long currentTime) {
        val service = cacheKey.getRegisteredService();
        val duration = getCacheDurationForServiceProvider(service, chainingMetadataResolver);
        if (duration >= 0) {
            LOGGER.trace("Metadata cache duration for [{}] is [{}]", service.getName(), duration);
            return duration;
        }
        LOGGER.trace("Metadata for [{}] does not define caching policies", service.getName());
        if (StringUtils.isNotBlank(service.getMetadataExpirationDuration())) {
            LOGGER.debug("Service [{}] defines a cache expiration duration of [{}]", service.getName(), service.getMetadataExpirationDuration());
            return Beans.newDuration(service.getMetadataExpirationDuration()).toNanos();
        }
        LOGGER.trace("Service [{}] does not define caching policies. Falling back onto default...", service.getName());
        return defaultExpiration;
    }

    @Override
    public long expireAfterUpdate(@NonNull final SamlRegisteredServiceCacheKey cacheKey,
                                  @NonNull final MetadataResolver chainingMetadataResolver,
                                  final long currentTime, final long currentDuration) {
        LOGGER.trace("Cache expiration duration after updates is set to [{}] nanoseconds", currentDuration);
        return currentDuration;
    }

    @Override
    public long expireAfterRead(@NonNull final SamlRegisteredServiceCacheKey cacheKey,
                                @NonNull final MetadataResolver chainingMetadataResolver,
                                final long currentTime, final long currentDuration) {
        LOGGER.trace("Cache expiration duration after reads is set to [{}] nanoseconds", currentDuration);
        return currentDuration;
    }

    /**
     * Gets cache duration for service provider.
     *
     * @param service                  the service
     * @param chainingMetadataResolver the chaining metadata resolver
     * @return the cache duration for service provider
     */
    protected long getCacheDurationForServiceProvider(final SamlRegisteredService service, final MetadataResolver chainingMetadataResolver) {
        try {
            if (StringUtils.isBlank(service.getServiceId())) {
                LOGGER.warn("Unable to determine duration for SAML service [{}] with no entity id", service.getName());
                return -1;
            }
            val set = new CriteriaSet();
            set.add(new EntityIdCriterion(service.getServiceId()));
            set.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            val entitySp = chainingMetadataResolver.resolveSingle(set);
            if (entitySp != null && entitySp.getCacheDuration() != null) {
                LOGGER.debug("Located cache duration [{}] specified in SP metadata for [{}]", entitySp.getCacheDuration(), entitySp.getEntityID());
                return TimeUnit.MILLISECONDS.toNanos(entitySp.getCacheDuration().toMillis());
            }

            set.clear();
            set.add(new EntityIdCriterion(service.getServiceId()));
            val entity = chainingMetadataResolver.resolveSingle(set);
            if (entity != null && entity.getCacheDuration() != null) {
                LOGGER.debug("Located cache duration [{}] specified in entity metadata for [{}]", entity.getCacheDuration(), entity.getEntityID());
                return TimeUnit.MILLISECONDS.toNanos(entity.getCacheDuration().toMillis());
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return -1;
    }
}
