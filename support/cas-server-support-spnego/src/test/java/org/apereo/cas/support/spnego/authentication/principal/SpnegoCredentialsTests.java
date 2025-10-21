package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Tag("Spnego")
class SpnegoCredentialsTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyToStringWithNoPrincipal() {
        val credentials = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        assertTrue(credentials.getId().contains("unknown"));
    }

    @Test
    void verifyToStringWithPrincipal() throws Throwable {
        val credentials = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test");
        credentials.setPrincipal(principal);
        assertEquals("test", credentials.getId());
    }

    /**
     * Make sure that when the Principal becomes populated / changes we return a new hash
     */
    @Test
    void verifyPrincipalAffectsHash() throws Throwable {
        val credential = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        val hash1 = credential.hashCode();
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test");
        credential.setPrincipal(principal);
        val hash2 = credential.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verifyToStringWithToken() throws Throwable {
        val credentials = new SpnegoCredential(new byte[16]);
        credentials.setNextToken(new byte[16]);
        assertFalse(MAPPER.writeValueAsString(credentials).contains("initToken"));
        assertFalse(MAPPER.writeValueAsString(credentials).contains("nextToken"));

    }

    @Test
    void verifyJsonWithToken() throws Throwable {
        val credentials = new SpnegoCredential(new byte[16]);
        credentials.setNextToken(new byte[16]);
        assertFalse(MAPPER.writeValueAsString(credentials).contains("initToken"));
        assertFalse(MAPPER.writeValueAsString(credentials).contains("nextToken"));
    }

}
