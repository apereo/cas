package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DateTimeUtils;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public record SamlRegisteredServiceMetadataExpirationPolicy(Duration defaultExpiration) implements Expiry<@NonNull SamlRegisteredServiceCacheKey, @NonNull CachedMetadataResolverResult> {
    @Override
    public long expireAfterCreate(
        final @NonNull SamlRegisteredServiceCacheKey cacheKey,
        final @NonNull CachedMetadataResolverResult cacheResult,
        final long currentTime) {
        val service = cacheKey.getRegisteredService();
        val duration = getCacheDurationForServiceProvider(service, cacheResult);
        if (duration >= 0) {
            LOGGER.trace("Metadata cache duration for [{}] is [{}]", service.getName(), duration);
            return duration;
        }
        LOGGER.trace("Metadata for [{}] does not define caching policies", service.getName());
        if (StringUtils.isNotBlank(service.getMetadataExpirationDuration())) {
            LOGGER.debug("Service [{}] defines a cache expiration duration of [{}]", service.getName(), service.getMetadataExpirationDuration());
            return Beans.newDuration(service.getMetadataExpirationDuration()).toNanos();
        }
        val expPolicy = service.getExpirationPolicy();
        if (expPolicy != null && StringUtils.isNotBlank(expPolicy.getExpirationDate())) {
            val expDate = DateTimeUtils.localDateTimeOf(expPolicy.getExpirationDate());
            @SuppressWarnings("JavaTimeDefaultTimeZone")
            val now = LocalDateTime.now(Clock.systemDefaultZone());
            val result = Duration.between(now, expDate).toNanos();
            LOGGER.trace("Using the difference between now [{}] and "
                         + "expiration date [{}] from the service expiration policy: [{}]", now, expDate, result);
            return result;
        }

        LOGGER.trace("Service [{}] does not define caching policies. Falling back onto default...", service.getName());
        return defaultExpiration.toNanos();
    }

    @Override
    public long expireAfterUpdate(
        final @NonNull SamlRegisteredServiceCacheKey cacheKey,
        final @NonNull CachedMetadataResolverResult cacheResult,
        final long currentTime, final long currentDuration) {
        LOGGER.trace("Cache expiration duration after updates is set to [{}] nanoseconds", currentDuration);
        return currentDuration;
    }

    @Override
    public long expireAfterRead(
        final @NonNull SamlRegisteredServiceCacheKey cacheKey,
        final @NonNull CachedMetadataResolverResult cacheResult,
        final long currentTime, final long currentDuration) {
        LOGGER.trace("Cache expiration duration after reads is set to [{}] nanoseconds", currentDuration);
        return currentDuration;
    }

    long getCacheDurationForServiceProvider(final SamlRegisteredService service,
                                            final CachedMetadataResolverResult cacheResult) {
        try {
            if (StringUtils.isBlank(service.getServiceId())) {
                LOGGER.warn("Unable to determine duration for SAML service [{}] with no entity id", service.getName());
                return -1;
            }
            val set = new CriteriaSet();
            set.add(new EntityIdCriterion(service.getServiceId()));
            set.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            val entitySp = cacheResult.getMetadataResolver().resolveSingle(set);
            if (entitySp != null && entitySp.getCacheDuration() != null) {
                LOGGER.debug("Located cache duration [{}] specified in SP metadata for [{}]", entitySp.getCacheDuration(), entitySp.getEntityID());
                return TimeUnit.MILLISECONDS.toNanos(entitySp.getCacheDuration().toMillis());
            }

            set.clear();
            set.add(new EntityIdCriterion(service.getServiceId()));
            val entity = cacheResult.getMetadataResolver().resolveSingle(set);
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
