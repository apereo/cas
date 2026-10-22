package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

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

    public RedisSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                       final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                       final CasRedisTemplate<String, SamlIdPMetadataDocument> redisTemplate,
                                       final ConfigurableApplicationContext applicationContext) {
        super(metadataCipherExecutor, metadataCache, applicationContext);
        this.redisTemplate = redisTemplate;
    }

    /**
     * Fetch the metadata document for the service, or the global document when the service has none of its own.
     * Metadata is only generated per service when asked for, so a service without its own document must use the
     * identity provider's global metadata and keys, as the JPA and MongoDb locators do; without this fallback the
     * signing key of every service provider resolves to nothing and SAML2 responses cannot be signed.
     *
     * @param registeredService the registered service
     * @return the metadata document, or null if there is none
     */
    @Override
    public @Nullable SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        if (registeredService.isPresent()) {
            val document = findDocumentFor(registeredService);
            if (document != null && document.isValid()) {
                return document;
            }
        }
        return findDocumentFor(Optional.empty());
    }

    private @Nullable SamlIdPMetadataDocument findDocumentFor(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = getAppliesToFor(registeredService);
        try (val keys = redisTemplate.scan(CAS_PREFIX + appliesTo + ":*")) {
            return keys.findFirst()
                .map(key -> redisTemplate.boundValueOps(key).get())
                .orElse(null);
        }
    }
}
