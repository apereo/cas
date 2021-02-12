package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.AbstractSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

/**
 * This is {@link RedisSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class RedisSamlIdPMetadataLocator extends AbstractSamlIdPMetadataLocator {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlIdPMetadataDocument.class.getSimpleName() + ':';

    private final transient RedisTemplate<String, SamlIdPMetadataDocument> redisTemplate;

    public RedisSamlIdPMetadataLocator(final CipherExecutor<String, String> metadataCipherExecutor,
                                       final Cache<String, SamlIdPMetadataDocument> metadataCache,
                                       final RedisTemplate<String, SamlIdPMetadataDocument> redisTemplate) {
        super(metadataCipherExecutor, metadataCache);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SamlIdPMetadataDocument fetchInternal(final Optional<SamlRegisteredService> registeredService) {
        val appliesTo = SamlIdPMetadataGenerator.getAppliesToFor(registeredService);
        val keys = redisTemplate.keys(CAS_PREFIX + appliesTo + ":*");
        if (keys != null && !keys.isEmpty()) {
            val redisKey = keys.iterator().next();
            return redisTemplate.boundValueOps(redisKey).get();
        }
        return null;
    }
}
