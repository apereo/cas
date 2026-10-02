package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityUniversalPromptCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DuoSecurity")
class DuoSecurityUniversalPromptCredentialTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val credential = new DuoSecurityUniversalPromptCredential(id, CoreAuthenticationTestUtils.getAuthentication());
        credential.setProviderId(id);
        assertNotNull(credential.getAuthentication());
        assertEquals(id, credential.getId());
        assertEquals(id, credential.getToken());
        assertEquals(id, credential.getProviderId());
    }

    @Test
    void verifyFirstFactorAuthenticationIsNotSerialized() {
        val credential = new DuoSecurityUniversalPromptCredential(UUID.randomUUID().toString(), CoreAuthenticationTestUtils.getAuthentication());
        credential.setProviderId("mfa-duo");

        val json = MAPPER.writeValueAsString(credential);
        assertFalse(json.contains("\"authentication\""), json);
        val read = (DuoSecurityUniversalPromptCredential) MAPPER.readValue(json, Credential.class);
        assertEquals(credential.getToken(), read.getToken());
        assertEquals("mfa-duo", read.getProviderId());
        assertNull(read.getAuthentication());

        val copy = SerializationUtils.<DuoSecurityUniversalPromptCredential>deserialize(SerializationUtils.serialize(credential));
        assertEquals(credential.getToken(), copy.getToken());
        assertNull(copy.getAuthentication());
    }

}
