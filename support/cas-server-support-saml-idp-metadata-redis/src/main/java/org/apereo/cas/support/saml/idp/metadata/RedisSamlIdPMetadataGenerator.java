package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;

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

    private final CasRedisTemplate<String, SamlIdPMetadataDocument> redisTemplate;

    public RedisSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context,
                                         final CasRedisTemplate<String, SamlIdPMetadataDocument> redisTemplate) {
        super(context);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(_ -> generate(Optional.empty()));
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument document,
                                                               final Optional<SamlRegisteredService> registeredService) {
        document.setAppliesTo(getAppliesToFor(registeredService));
        val redisKey = CAS_PREFIX + document.getAppliesTo() + ':' + document.getId();
        redisTemplate.boundValueOps(redisKey).set(document);
        return document;
    }

    @Override
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }

    @Override
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) throws Exception {
        return generateCertificateAndKey();
    }
}
