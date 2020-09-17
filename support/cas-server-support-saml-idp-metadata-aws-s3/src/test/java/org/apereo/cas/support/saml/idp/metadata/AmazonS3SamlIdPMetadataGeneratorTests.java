package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.config.AmazonS3SamlIdPMetadataConfiguration;
import org.apereo.cas.config.AmazonS3SamlMetadataConfiguration;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonS3SamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    AmazonS3SamlMetadataConfiguration.class,
    AmazonS3SamlIdPMetadataConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.amazon-s3.idp-metadata-bucket-name=thebucket",
    "cas.authn.saml-idp.metadata.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-access-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-secret-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
})
@EnabledIfPortOpen(port = 4566)
@Tag("AmazonWebServices")
public class AmazonS3SamlIdPMetadataGeneratorTests {
    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    private SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Test
    public void verifyOperation() {
        samlIdPMetadataGenerator.generate(Optional.empty());
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
    }

    @Test
    public void verifyService() {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        val registeredService = Optional.of(service);

        samlIdPMetadataGenerator.generate(registeredService);
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
    }
}
