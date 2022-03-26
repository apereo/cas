package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Tag("Spnego")
public class SpnegoCredentialsTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    public void verifyToStringWithNoPrincipal() {
        val credentials = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        assertTrue(credentials.getId().contains("unknown"));
    }

    @Test
    public void verifyToStringWithPrincipal() {
        val credentials = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test");
        credentials.setPrincipal(principal);
        assertEquals("test", credentials.getId());
    }

    /**
     * Make sure that when the Principal becomes populated / changes we return a new hash
     */
    @Test
    public void verifyPrincipalAffectsHash() {
        val credential = new SpnegoCredential(ArrayUtils.EMPTY_BYTE_ARRAY);
        val hash1 = credential.hashCode();
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("test");
        credential.setPrincipal(principal);
        val hash2 = credential.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void verifyToStringWithToken() {
        val credentials = new SpnegoCredential(new byte[16]);
        credentials.setNextToken(new byte[16]);
        assertThat(credentials.toString(), not(containsString("initToken")));
        assertThat(credentials.toString(), not(containsString("nextToken")));
    }

    @Test
    public void verifyJsonWithToken() throws Exception {
        val credentials = new SpnegoCredential(new byte[16]);
        credentials.setNextToken(new byte[16]);
        assertThat(MAPPER.writeValueAsString(credentials), not(containsString("initToken")));
        assertThat(MAPPER.writeValueAsString(credentials), not(containsString("nextToken")));
    }

}