package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseDynamoDbSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ResourceLock("samlIdentityProviderDynamoDbMetadataTable")
@Tag("DynamoDb")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.dynamodb.idp-metadata-table-name=saml-idp-metadata")
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbSamlIdPMetadataLocatorTests extends BaseDynamoDbSamlMetadataTests {
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
