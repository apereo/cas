package org.apereo.cas.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HttpUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class HttpUtilsTests {

    @Test
    public void verifyExec() {
        assertNotNull(HttpUtils.getHttpClient());
        assertNull(HttpUtils.execute("http://localhost:1234", "GET", Map.of()));
        assertNull(HttpUtils.executeGet("http://localhost:1234", "user", "password", Map.of()));
        assertNull(HttpUtils.executeDelete("http://localhost:1234", "user", "password", Map.of(), Map.of()));
    }

    @Test
    public void verifyClose() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                HttpUtils.close(null);
            }
        });
    }

}
