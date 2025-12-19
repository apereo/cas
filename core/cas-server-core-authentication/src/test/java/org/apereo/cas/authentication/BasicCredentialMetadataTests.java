package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
@Tag("AuthenticationMetadata")
class BasicCredentialMetadataTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "BasicCredentialMetadata.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeABasicCredentialMetaDataToJson() {
        val credentialMetadata = new BasicCredentialMetadata(new UsernamePasswordCredential());
        credentialMetadata.putProperty("key", "value").putProperties(Map.of("one", "two"));
        credentialMetadata.putProperty(CredentialMetadata.PROPERTY_GEO_LOCATION,
            new GeoLocationRequest(20, 40));
        assertTrue(credentialMetadata.containsProperty("one"));
        assertNotNull(credentialMetadata.getProperty("key", String.class));
        MAPPER.writeValue(JSON_FILE, credentialMetadata);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, BasicCredentialMetadata.class);
        assertEquals(credentialMetadata, credentialMetaDataRead);
        credentialMetadata.removeProperty("one");
        assertFalse(credentialMetadata.containsProperty("one"));
    }
}

