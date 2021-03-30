package org.apereo.cas.support.saml;

import org.apereo.cas.couchdb.saml.CouchDbSamlMetadataDocument;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbSamlMetadataDocumentTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("CouchDb")
public class CouchDbSamlMetadataDocumentTests {
    @Test
    public void verifyOperation() {
        val doc = CouchDbSamlMetadataDocument.builder()
            .signature("signature")
            .id(1)
            .name("name")
            .value("value")
            .build();

        val doc2 = CouchDbSamlMetadataDocument.builder()
            .signature("signature2")
            .id(2)
            .name("name2")
            .value("value2")
            .build();

        val result = doc.merge(doc2);
        assertEquals(2, result.getId());
        assertEquals("name2", result.getName());
        assertEquals("value2", result.getValue());
        assertEquals("signature2", result.getSignature());
    }
}
