package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.passwordless.accounts.groovy.location=classpath:PasswordlessAccount.groovy")
@Tag("Groovy")
public class GroovyPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    public void verifyAction() {
        val user = passwordlessUserAccountStore.findUser("casuser");
        assertTrue(user.isPresent());
    }

    @Test
    public void verifyAcct() throws Exception {
        var user = PasswordlessUserAccount.builder().username("user1").build();
        assertNotNull(user.getId());
        user.setId("cas2");
        assertEquals("cas2", user.getId());

        val json = MAPPER.writeValueAsString(user);
        assertFalse(json.contains("\"id\""));
    }
}
