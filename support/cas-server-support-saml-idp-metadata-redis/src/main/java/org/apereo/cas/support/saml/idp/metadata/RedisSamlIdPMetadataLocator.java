package org.apereo.cas.support.saml.idp.metadata;


import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

/**
 * This is {@link RedisSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Monitorable
public class RedisSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlIdPMetadataDocument.class.getSimpleName() + ':';

    private final CasRedisTemplate<String, SamlIdPMetadataDocument> redisTemplate;

    private final long scanCount;

    public RedisSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                       final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                       final CasRedisTemplate<String, SamlIdPMetadataDocument> redisTemplate,
                                       final ConfigurableApplicationContext applicationContext,
                                       final long scanCount) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.redisTemplate = redisTemplate;
        this.scanCount = scanCount;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = getAppliesToFor(registeredService);
        try (val keys = redisTemplate.scan(CAS_PREFIX + appliesTo + ":*", this.scanCount)) {
            return keys.findFirst()
                .map(key -> redisTemplate.boundValueOps(key).get())
                .orElse(null);
        }
    }
}
