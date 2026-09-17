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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ConfigurableApplicationContext;
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
    class ContainmentTests {
        private static final List<String> ARTIFACT_NAMES = List.of("idp-metadata.xml", "idp-signing.crt",
            "idp-signing.key", "idp-encryption.crt", "idp-encryption.key");

        @TempDir
        private Path directory;

        @Test
        void verifyTraversalFallsBackToGlobalArtifacts() throws Throwable {
            val metadata = directory.resolve("metadata");
            writeArtifacts(metadata, "global");
            writeArtifacts(directory.resolve("metadata-other-1000"), "outside");
            val service = new SamlRegisteredService();
            service.setId(1000);
            service.setName("../metadata-other");
            assertArtifacts(metadata, service, "global");
        }

        @Test
        void verifySymlinkEscapeFallsBackToGlobalArtifacts() throws Throwable {
            val metadata = directory.resolve("metadata");
            val outside = directory.resolve("outside");
            writeArtifacts(metadata, "global");
            writeArtifacts(outside, "outside");
            Files.createSymbolicLink(metadata.resolve("service-1000"), outside);
            val service = new SamlRegisteredService();
            service.setId(1000);
            service.setName("service");
            assertArtifacts(metadata, service, "global");
        }

        @ParameterizedTest
        @ValueSource(strings = {"service", "partners/service"})
        void verifyExistingServiceDirectory(final String serviceName) throws Throwable {
            val metadata = directory.resolve("metadata");
            writeArtifacts(metadata, "global");
            writeArtifacts(metadata.resolve(serviceName + "-1000"), "service");
            val service = new SamlRegisteredService();
            service.setId(1000);
            service.setName(serviceName);
            assertArtifacts(metadata, service, "service");
        }

        @Test
        void verifyExplicitLocationOutsideMetadataRoot() throws Throwable {
            val metadata = directory.resolve("metadata");
            val outside = directory.resolve("outside");
            writeArtifacts(metadata, "global");
            writeArtifacts(outside, "explicit");
            val service = new SamlRegisteredService();
            service.setId(1000);
            service.setName("../service");
            service.setIdpMetadataLocation(outside.toUri().toString());
            assertArtifacts(metadata, service, "explicit");
        }

        @Test
        void verifySymlinkedMetadataRoot() throws Throwable {
            val metadata = directory.resolve("metadata");
            val linkedMetadata = directory.resolve("linked-metadata");
            writeArtifacts(metadata, "global");
            writeArtifacts(metadata.resolve("service-1000"), "service");
            Files.createSymbolicLink(linkedMetadata, metadata);
            val service = new SamlRegisteredService();
            service.setId(1000);
            service.setName("service");
            assertArtifacts(linkedMetadata, service, "service");
        }

        private static void writeArtifacts(final Path location, final String prefix) throws IOException {
            Files.createDirectories(location);
            for (val artifact : ARTIFACT_NAMES) {
                Files.writeString(location.resolve(artifact), prefix + ':' + artifact);
            }
        }

        private static void assertArtifacts(final Path metadata, final SamlRegisteredService service,
                                            final String prefix) throws Throwable {
            val locator = new FileSystemSamlIdPMetadataLocator(CipherExecutor.noOpOfStringToString(),
                metadata.toFile(), mock(Cache.class), mock(ConfigurableApplicationContext.class));
            val registeredService = Optional.of(service);
            val artifacts = List.of(locator.resolveMetadata(registeredService), locator.resolveSigningCertificate(registeredService),
                locator.resolveSigningKey(registeredService), locator.resolveEncryptionCertificate(registeredService),
                locator.resolveEncryptionKey(registeredService));
            for (var i = 0; i < artifacts.size(); i++) {
                assertEquals(prefix + ':' + ARTIFACT_NAMES.get(i), artifacts.get(i).getContentAsString(StandardCharsets.UTF_8));
            }
        }
    }

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
