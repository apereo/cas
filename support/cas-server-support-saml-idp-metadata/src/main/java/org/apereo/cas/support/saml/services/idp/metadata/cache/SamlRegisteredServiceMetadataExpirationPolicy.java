package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
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
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.common.TimeBoundSAMLObject;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicy}.
 * <p>
 * The lifetime of cached metadata is the {@code cacheDuration} the metadata asks for, kept between the given minimum
 * and maximum; otherwise the expiration duration or policy of the service; otherwise the default. Whichever applies,
 * cached metadata never outlives the {@code validUntil} of the entity it was loaded for, or of the documents that
 * enclose it, as long as that instant lies in the future: the metadata is loaded again once it is no longer valid,
 * rather than turning every request away until the entry expires.
 *
 * @author Misagh Moayyed
 * @param defaultExpiration    the default lifetime
 * @param minimumCacheDuration the shortest lifetime a metadata {@code cacheDuration} may ask for
 * @param maximumCacheDuration the longest lifetime a metadata {@code cacheDuration} may ask for
 * @since 5.2.0
 */
@Slf4j
public record SamlRegisteredServiceMetadataExpirationPolicy(Duration defaultExpiration, Duration minimumCacheDuration,
                                                            Duration maximumCacheDuration)
    implements Expiry<SamlRegisteredServiceCacheKey, @NonNull CachedMetadataResolverResult> {

    public SamlRegisteredServiceMetadataExpirationPolicy(final Duration defaultExpiration) {
        this(defaultExpiration, Duration.ZERO, Duration.ofNanos(Long.MAX_VALUE));
    }

    @Override
    public long expireAfterCreate(
        final @NonNull SamlRegisteredServiceCacheKey cacheKey,
        final @NonNull CachedMetadataResolverResult cacheResult,
        final long currentTime) {
        val lifetime = getLifetime(cacheKey, cacheResult);
        if (lifetime <= 0) {
            return lifetime;
        }
        return getValidUntil(cacheKey, cacheResult)
            .map(validUntil -> Duration.between(Instant.now(Clock.systemUTC()), validUntil))
            .filter(remaining -> remaining.isPositive() && remaining.compareTo(Duration.ofNanos(lifetime)) < 0)
            .map(remaining -> {
                LOGGER.debug("Metadata for [{}] is valid for [{}], less than its cache lifetime", cacheKey.getRegisteredService().getName(), remaining);
                return remaining.toNanos();
            })
            .orElse(lifetime);
    }

    private long getLifetime(final SamlRegisteredServiceCacheKey cacheKey, final CachedMetadataResolverResult cacheResult) {
        val service = cacheKey.getRegisteredService();
        val duration = getCacheDurationForServiceProvider(service, cacheResult);
        if (duration >= 0) {
            val lifetime = Math.min(Math.max(duration, minimumCacheDuration.toNanos()), maximumCacheDuration.toNanos());
            LOGGER.trace("Metadata cache duration for [{}] is [{}], kept at [{}]", service.getName(), duration, lifetime);
            return lifetime;
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
        return expireAfterCreate(cacheKey, cacheResult, currentTime);
    }

    @Override
    public long expireAfterRead(
        final @NonNull SamlRegisteredServiceCacheKey cacheKey,
        final @NonNull CachedMetadataResolverResult cacheResult,
        final long currentTime, final long currentDuration) {
        LOGGER.trace("Cache expiration duration after reads is set to [{}] nanoseconds", currentDuration);
        return currentDuration;
    }

    /**
     * Gets the earliest {@code validUntil} of the entity the metadata was loaded for, which is the entity of the cache
     * key or else the entity id of the service, and of the documents that enclose that entity.
     *
     * @param cacheKey    the cache key
     * @param cacheResult the cached metadata
     * @return the earliest validity limit, if any
     */
    Optional<Instant> getValidUntil(final SamlRegisteredServiceCacheKey cacheKey, final CachedMetadataResolverResult cacheResult) {
        try {
            val entityId = Optional.ofNullable(cacheKey.getCriteriaSet())
                .map(criteria -> criteria.get(EntityIdCriterion.class))
                .map(EntityIdCriterion::getEntityId)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> cacheKey.getRegisteredService().getServiceId());
            if (StringUtils.isBlank(entityId)) {
                return Optional.empty();
            }
            var validUntil = (Instant) null;
            var current = (XMLObject) cacheResult.getMetadataResolver().resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId)));
            while (current != null) {
                if (current instanceof final TimeBoundSAMLObject timeBound) {
                    val instant = timeBound.getValidUntil();
                    if (instant != null && (validUntil == null || instant.isBefore(validUntil))) {
                        validUntil = instant;
                    }
                }
                current = current.getParent();
            }
            return Optional.ofNullable(validUntil);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
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
