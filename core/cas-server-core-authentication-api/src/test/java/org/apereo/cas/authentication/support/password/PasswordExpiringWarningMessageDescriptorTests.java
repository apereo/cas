package org.apereo.cas.authentication.support.password;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordExpiringWarningMessageDescriptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("PasswordOps")
class PasswordExpiringWarningMessageDescriptorTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() {
        val d = new PasswordExpiringWarningMessageDescriptor("DefaultMessage", 30);
        assertEquals(30, d.getDaysToExpiration());
        assertEquals("DefaultMessage", d.getDefaultMessage());
    }

    @Test
    void verifySerialization() throws Throwable {
        val d = new PasswordExpiringWarningMessageDescriptor("DefaultMessage", 30);
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        handler.addMessageDescriptor(d);
        val credential = new UsernamePasswordCredential("casuser", "resusac");
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertNotNull(result.getWarnings());

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, result);
        val read = MAPPER.readValue(jsonFile, AuthenticationHandlerExecutionResult.class);
        assertEquals(result, read);
    }
}
