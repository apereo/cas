package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbSamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class MongoDbSamlIdPMetadataLocator implements SamlIdPMetadataLocator {
    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;
    private final CipherExecutor metadataCipherExecutor;

    @Override
    public Resource getSigningCertificate() {
        return null;
    }

    @Override
    public Resource getSigningKey() {
        return null;
    }

    @Override
    public Resource getMetadata() {
        return null;
    }

    @Override
    public Resource getEncryptionCertificate() {
        return null;
    }

    @Override
    public Resource getEncryptionKey() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean exists() {
        return false;
    }
}
