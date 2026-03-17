package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
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
}
