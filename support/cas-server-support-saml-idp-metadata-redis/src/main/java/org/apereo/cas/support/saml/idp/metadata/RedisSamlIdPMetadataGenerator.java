package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

/**
 * This is {@link RedisSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class RedisSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlIdPMetadataDocument.class.getSimpleName() + ':';

    private final transient RedisTemplate<String, SamlIdPMetadataDocument> redisTemplate;

    public RedisSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                         final RedisTemplate<String, SamlIdPMetadataDocument> redisTemplate) {
        super(context);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        generate(Optional.empty());
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument document,
                                                               final Optional<SamlRegisteredService> registeredService) {
        document.setAppliesTo(SamlIdPMetadataGenerator.getAppliesToFor(registeredService));
        val redisKey = CAS_PREFIX + document.getAppliesTo() + ':' + document.getId();
        redisTemplate.boundValueOps(redisKey).set(document);
        return document;
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        return generateCertificateAndKey();
    }
}
