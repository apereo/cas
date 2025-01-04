package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.config.CasAmazonS3SamlMetadataAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonS3SamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasAmazonS3SamlMetadataAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.amazon-s3.idp-metadata-bucket-name=thebucket",
    "cas.authn.saml-idp.metadata.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.authn.saml-idp.metadata.amazon-s3.region=us-east-1",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-access-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-secret-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.alg=A128CBC-HS256",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
class AmazonS3SamlIdPMetadataGeneratorTests {
    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    private SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier("amazonS3Client")
    private S3Client amazonS3Client;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = Optional.<SamlRegisteredService>empty();
        samlIdPMetadataGenerator.generate(registeredService);
        samlIdPMetadataCache.invalidateAll();

        /*
         * Tamper with metadata manually.
         */
        val bucketNameToUse = AmazonS3SamlIdPMetadataUtils.determineBucketNameFor(registeredService,
            casProperties.getAuthn().getSamlIdp().getMetadata().getAmazonS3().getIdpMetadataBucketName(), amazonS3Client);
        val document = new SamlIdPMetadataDocument();
        document.setId(RandomUtils.nextLong());
        AmazonS3SamlIdPMetadataUtils.putSamlIdPMetadataIntoBucket(amazonS3Client, document, bucketNameToUse);
        assertThrows(IllegalArgumentException.class, () -> samlIdPMetadataGenerator.generate(registeredService));
        samlIdPMetadataCache.invalidateAll();
        assertEquals(ResourceUtils.EMPTY_RESOURCE, samlIdPMetadataLocator.resolveMetadata(registeredService));

        samlIdPMetadataGenerator.generate(registeredService);
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
    }

    @Test
    void verifyService() throws Throwable {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        val registeredService = Optional.of(service);

        samlIdPMetadataGenerator.generate(registeredService);
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
    }
}
