package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseJpaSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.Optional;
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

}
