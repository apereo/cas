package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleCloudStorageSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("GCP")
class GoogleCloudStorageSamlIdPMetadataLocatorTests extends BaseGoogleCloudStorageSamlMetadataTests {

    @Test
    void verifySigningKeyWithoutService() throws Throwable {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }

    @Test
    void verifyServiceWithoutOwnMetadataUsesGlobalDocument() throws Throwable {
        val service = new SamlRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName("ServiceWithoutMetadata");
        service.setServiceId("https://sp.example.org");
        val global = samlIdPMetadataLocator.fetch(Optional.empty());
        assertNotNull(global);
        val document = samlIdPMetadataLocator.fetch(Optional.of(service));
        assertNotNull(document);
        assertEquals(global.getSigningKey(), document.getSigningKey());
        assertTrue(samlIdPMetadataLocator.resolveSigningKey(Optional.of(service)).contentLength() > 0);
    }
}
