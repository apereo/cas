package org.apereo.cas.authentication.support.password;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordExpiringWarningMessageDescriptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("PasswordOps")
public class PasswordExpiringWarningMessageDescriptorTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "PasswordExpiringWarningMessageDescriptor.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyOperation() {
        val d = new PasswordExpiringWarningMessageDescriptor("DefaultMessage", 30);
        assertEquals(30, d.getDaysToExpiration());
        assertEquals("DefaultMessage", d.getDefaultMessage());
    }

    @Test
    public void verifySerialization() throws Exception {
        val d = new PasswordExpiringWarningMessageDescriptor("DefaultMessage", 30);
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
        handler.addMessageDescriptor(d);
        val credential = new UsernamePasswordCredential("casuser", "resusac");
        val result = handler.authenticate(credential);
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertNotNull(result.getWarnings());

        MAPPER.writeValue(JSON_FILE, result);
        val read = MAPPER.readValue(JSON_FILE, AuthenticationHandlerExecutionResult.class);
        assertEquals(result, read);
    }
}
