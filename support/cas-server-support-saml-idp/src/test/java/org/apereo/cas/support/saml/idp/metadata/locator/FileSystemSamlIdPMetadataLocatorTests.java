package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FileSystemSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
class FileSystemSamlIdPMetadataLocatorTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.file-system.location=file:src/test/resources/metadata-encrypted-keys"
    })
    class EncryptedKeysTests extends BaseSamlIdPConfigurationTests {
        static {
            System.setProperty(CasConfigurationJasyptCipherExecutor.JasyptEncryptionParameters.PASSWORD.getPropertyName(), "P@$$w0rd");
        }

        @Test
        void verifyOperation() throws Throwable {
            samlIdPMetadataLocator.initialize();
            assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
            assertTrue(samlIdPMetadataLocator.exists(Optional.empty()));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata72"
    })
    class DefaultTests extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyUnknownDirectory() {
            val locator = new FileSystemSamlIdPMetadataLocator(CipherExecutor.noOpOfStringToString(),
                new File("/#**??#"), mock(Cache.class), applicationContext);
            assertThrows(IllegalArgumentException.class, locator::initialize);
        }

        @Test
        void verifyOperation() throws Throwable {
            samlIdPMetadataLocator.initialize();
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
            assertTrue(samlIdPMetadataLocator.exists(Optional.empty()));
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

        @Test
        void verifyMetadataPerService() throws Throwable {
            val service = new SamlRegisteredService();
            service.setName("TestShib");
            service.setId(2000);
            service.setIdpMetadataLocation("file:src/test/resources/metadata/ObjectSignerTest-1000");
            val registeredService = Optional.of(service);

            assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
        }
    }
}
