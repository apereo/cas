package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.function.FunctionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link MongoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final MongoOperations mongoTemplate;

    private final String collectionName;

    public MongoDbSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
                                           final MongoOperations mongoTemplate, final String collectionName) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public void afterPropertiesSet() {
        FunctionUtils.doUnchecked(_ -> generate(Optional.empty()));
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        doc.setAppliesTo(getAppliesToFor(registeredService));
        return this.mongoTemplate.save(doc, this.collectionName);
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
