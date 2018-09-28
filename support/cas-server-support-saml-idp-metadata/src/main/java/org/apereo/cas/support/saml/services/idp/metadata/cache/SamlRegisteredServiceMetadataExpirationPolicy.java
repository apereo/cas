package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlRegisteredServiceMetadataExpirationPolicy implements Expiry<SamlRegisteredServiceCacheKey, MetadataResolver> {
    private final long defaultExpiration;
    
    public SamlRegisteredServiceMetadataExpirationPolicy(final long metadataCacheExpirationMinutes) {
        this.defaultExpiration = TimeUnit.MINUTES.toNanos(metadataCacheExpirationMinutes);
    }

    @Override
    public long expireAfterCreate(@Nonnull final SamlRegisteredServiceCacheKey cacheKey,
                                  @Nonnull final MetadataResolver chainingMetadataResolver, 
                                  final long currentTime) {
        final SamlRegisteredService service = cacheKey.getRegisteredService();
        final long duration = getCacheDurationForServiceProvider(service, chainingMetadataResolver);
        if (duration >= 0) {
            return duration;
        }
        LOGGER.debug("Metadata for [{}] does not define caching policies", service.getName());
        if (StringUtils.isNotBlank(service.getMetadataExpirationDuration())) {
            LOGGER.debug("Service [{}] defines a cache expiration duration of [{}]", service.getName(), service.getMetadataExpirationDuration());
            return Beans.newDuration(service.getMetadataExpirationDuration()).toNanos();
        }
        LOGGER.debug("Service [{}] does not define caching policies. Falling back onto default...", service.getName());
        return defaultExpiration;
    }

    private static long getCacheDurationForServiceProvider(final SamlRegisteredService service, final MetadataResolver chainingMetadataResolver) {
        try {
            final CriteriaSet set = new CriteriaSet();
            set.add(new EntityIdCriterion(service.getServiceId()));
            set.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            final EntityDescriptor entitySp = chainingMetadataResolver.resolveSingle(set);
            if (entitySp != null && entitySp.getCacheDuration() != null) {
                LOGGER.debug("Located cache duration [{}] specified in SP metadata for [{}]", entitySp.getCacheDuration(), entitySp.getEntityID());
                return TimeUnit.MILLISECONDS.toNanos(entitySp.getCacheDuration());
            }
            
            set.clear();
            set.add(new EntityIdCriterion(service.getServiceId()));
            final EntityDescriptor entity = chainingMetadataResolver.resolveSingle(set);
            if (entity != null && entity.getCacheDuration() != null) {
                LOGGER.debug("Located cache duration [{}] specified in entity metadata for [{}]", entity.getCacheDuration(), entity.getEntityID());
                return TimeUnit.MILLISECONDS.toNanos(entity.getCacheDuration());
            }
        } catch(final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public long expireAfterUpdate(@Nonnull final SamlRegisteredServiceCacheKey cacheKey,
                                  @Nonnull final MetadataResolver chainingMetadataResolver,
                                  final long currentTime, final long currentDuration) {
        LOGGER.debug("Cache expiration duration after updates is set to [{}]", currentDuration);
        return currentDuration;
    }

    @Override
    public long expireAfterRead(@Nonnull final SamlRegisteredServiceCacheKey cacheKey,
                                @Nonnull final MetadataResolver chainingMetadataResolver, 
                                final long currentTime, final long currentDuration) {
        LOGGER.debug("Cache expiration duration after reads is set to [{}]", currentDuration);
        return currentDuration;
    }
}
