package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlRegisteredServiceMetadataExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SamlRegisteredServiceMetadataExpirationPolicy implements Expiry<SamlRegisteredService, MetadataResolver> {
    private final long defaultExpiration;
    
    public SamlRegisteredServiceMetadataExpirationPolicy(final long metadataCacheExpirationMinutes) {
        this.defaultExpiration = TimeUnit.MINUTES.toNanos(metadataCacheExpirationMinutes);
    }

    @Override
    public long expireAfterCreate(@Nonnull final SamlRegisteredService service, 
                                  @Nonnull final MetadataResolver chainingMetadataResolver, 
                                  final long currentTime) {
        
        if (StringUtils.isNotBlank(service.getMetadataExpirationDuration())) {
            final Duration duration = Beans.newDuration(service.getMetadataLocation());
            return duration.toNanos();
        }
        return defaultExpiration;
    }

    @Override
    public long expireAfterUpdate(@Nonnull final SamlRegisteredService service,
                                  @Nonnull final MetadataResolver chainingMetadataResolver,
                                  final long currentTime, final long currentDuration) {
        return currentDuration;
    }

    @Override
    public long expireAfterRead(@Nonnull final SamlRegisteredService service, 
                                @Nonnull final MetadataResolver chainingMetadataResolver, 
                                final long currentTime, final long currentDuration) {
        return currentDuration;
    }
}
