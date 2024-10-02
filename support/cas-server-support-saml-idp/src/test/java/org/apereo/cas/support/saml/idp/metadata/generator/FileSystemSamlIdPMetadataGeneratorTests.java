package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileSystemSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
class FileSystemSamlIdPMetadataGeneratorTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.core.cache-expiration=0",
        "cas.authn.saml-idp.metadata.file-system.sign-metadata=true",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata456"
    })
    class DefaultTests extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyOperation() throws Throwable {
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
            val metadata = samlIdPMetadataLocator.resolveMetadata(Optional.empty());
            assertNotNull(metadata);
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.core.cache-expiration=0",
        "cas.authn.saml-idp.metadata.file-system.sign-metadata=true",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata599"
    })
    class ServiceTests extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyOperation() throws Throwable {
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));

            val service = new SamlRegisteredService();
            service.setName(RandomUtils.randomAlphabetic(12));
            service.setId(RandomUtils.nextInt());
            val registeredService = Optional.of(service);

            samlIdPMetadataGenerator.generate(registeredService);
            assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
        }
    }
}
