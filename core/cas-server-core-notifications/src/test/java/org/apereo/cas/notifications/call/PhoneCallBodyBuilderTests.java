package org.apereo.cas.notifications.call;

import module java.base;
import org.apereo.cas.configuration.model.support.phone.PhoneProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PhoneCallBodyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SMS")
class PhoneCallBodyBuilderTests {
    @Test
    void verifyOperation() {
        val body = PhoneCallBodyBuilder.builder()
            .properties(new PhoneProperties().setText("Hello ${name}"))
            .parameters(Map.of("name", "CAS")).build().get();
        assertEquals("Hello CAS", body);
    }

    @Test
    void verifyNoBody() {
        val body = PhoneCallBodyBuilder.builder()
            .properties(new PhoneProperties()).build().get();
        assertTrue(body.isEmpty());
    }

    @Test
    void verifyFileBody() throws Throwable {
        val file = Files.createTempFile("phonecall", ".txt");
        Files.writeString(file.toAbsolutePath(), "Hello CAS");
        val body = PhoneCallBodyBuilder.builder()
            .properties(new PhoneProperties().setText(file.toAbsolutePath().toString()))
            .build().get();
        assertEquals("Hello CAS", body);
    }
}
