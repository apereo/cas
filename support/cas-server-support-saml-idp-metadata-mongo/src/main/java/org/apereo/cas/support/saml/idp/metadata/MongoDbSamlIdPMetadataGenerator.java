package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.idp.metadata.generator.BaseSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

/**
 * This is {@link MongoDbSamlIdPMetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class MongoDbSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    private final transient MongoTemplate mongoTemplate;

    private final String collectionName;

    public MongoDbSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext,
                                           final MongoTemplate mongoTemplate, final String collectionName) {
        super(samlIdPMetadataGeneratorConfigurationContext);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    private static String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        if (result.isPresent()) {
            val registeredService = result.get();
            return registeredService.getName() + '-' + registeredService.getId();
        }
        return "CAS";
    }

    @Override
    public void afterPropertiesSet() {
        generate(Optional.empty());
    }

    @Override
    protected SamlIdPMetadataDocument finalizeMetadataDocument(final SamlIdPMetadataDocument doc,
                                                               final Optional<SamlRegisteredService> registeredService) {
        doc.setAppliesTo(getAppliesToFor(registeredService));
        return this.mongoTemplate.save(doc, this.collectionName);
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
