package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseDynamoDbSamlMetadataTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("DynamoDb")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.dynamodb.idp-metadata-table-name=saml-idp-metadata")
@EnabledIfListeningOnPort(port = 8000)
class DynamoDbSamlIdPMetadataLocatorTests extends BaseDynamoDbSamlMetadataTests {
    @Test
    void verifySigningKeyWithoutService() throws Throwable {
        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
