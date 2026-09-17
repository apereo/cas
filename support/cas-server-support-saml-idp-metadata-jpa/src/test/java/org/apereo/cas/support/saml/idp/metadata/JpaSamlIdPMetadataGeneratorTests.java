package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseJpaSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("JDBC")
class JpaSamlIdPMetadataGeneratorTests {

    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.jpa.idp-metadata-enabled=true",
        "cas.authn.saml-idp.metadata.jpa.crypto.enabled=false",
        "cas.authn.saml-idp.metadata.jpa.ddl-auto=create-drop",
        "cas.jdbc.show-sql=false"
    })
    @Nested
    class NoCipherJpaSamlIdPMetadataGeneratorTests extends BaseJpaSamlMetadataTests {
        @Test
        void verifyOperation() throws Throwable {
            this.samlIdPMetadataGenerator.generate(Optional.empty());
            assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.jpa.idp-metadata-enabled=true",
        "cas.authn.saml-idp.metadata.jpa.ddl-auto=create-drop",
        "cas.jdbc.show-sql=false"
    })
    @Nested
    class DefaultJpaSamlIdPMetadataGeneratorTests extends BaseJpaSamlMetadataTests {
        @Test
        void verifyOperation() throws Throwable {
            this.samlIdPMetadataGenerator.generate(Optional.empty());
            assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
            assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
        }

        @Test
        void verifyServiceIsServedTheGlobalDocument() throws Throwable {
            val service = new SamlRegisteredService();
            service.setName("TestShib");
            service.setId(1000);
            val registeredService = Optional.of(service);

            assertFalse(samlIdPMetadataLocator.shouldGenerateMetadataFor(registeredService));
            assertNotNull(samlIdPMetadataGenerator.generate(registeredService));

            val document = samlIdPMetadataLocator.fetch(registeredService);
            assertNotNull(document);
            assertEquals(samlIdPMetadataLocator.getAppliesToFor(Optional.empty()), document.getAppliesTo());

            val globalMetadata = contentOf(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
            assertFalse(globalMetadata.isBlank());
            assertEquals(globalMetadata, contentOf(samlIdPMetadataLocator.resolveMetadata(registeredService)));

            assertEquals(contentOf(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty())),
                contentOf(samlIdPMetadataLocator.resolveSigningCertificate(registeredService)));
            assertEquals(contentOf(samlIdPMetadataLocator.resolveSigningKey(Optional.empty())),
                contentOf(samlIdPMetadataLocator.resolveSigningKey(registeredService)));
            assertEquals(contentOf(samlIdPMetadataLocator.resolveEncryptionCertificate(Optional.empty())),
                contentOf(samlIdPMetadataLocator.resolveEncryptionCertificate(registeredService)));
            assertEquals(contentOf(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty())),
                contentOf(samlIdPMetadataLocator.resolveEncryptionKey(registeredService)));
        }

        private String contentOf(final Resource resource) throws Exception {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        }
    }

}
