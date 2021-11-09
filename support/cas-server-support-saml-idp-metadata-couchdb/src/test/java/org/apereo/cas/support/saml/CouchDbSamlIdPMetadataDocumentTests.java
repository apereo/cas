package org.apereo.cas.support.saml;

import org.apereo.cas.couchdb.saml.CouchDbSamlIdPMetadataDocument;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbSamlIdPMetadataDocumentTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("CouchDb")
public class CouchDbSamlIdPMetadataDocumentTests {
    @Test
    public void verifyOperation() {
        val doc = CouchDbSamlIdPMetadataDocument.builder()
            .encryptionCertificate("encryptionCertificate")
            .signingCertificate("signingCertificate")
            .signingKey("signingKey")
            .encryptionKey("encryptionKey")
            .build();

        val doc2 = CouchDbSamlIdPMetadataDocument.builder()
            .encryptionCertificate("encryptionCertificate2")
            .signingCertificate("signingCertificate2")
            .signingKey("signingKey2")
            .encryptionKey("encryptionKey2")
            .id(2)
            .build();

        val result = doc.merge(doc2);
        assertEquals(2, result.getId());
        assertEquals("encryptionCertificate2", result.getEncryptionCertificate());
        assertEquals("signingCertificate2", result.getSigningCertificate());
        assertEquals("signingKey2", result.getSigningKey());
        assertEquals("encryptionKey2", result.getEncryptionKey());
    }
}
