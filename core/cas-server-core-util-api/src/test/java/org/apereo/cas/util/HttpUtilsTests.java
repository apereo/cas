package org.apereo.cas.util;

import lombok.val;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertNull(HttpUtils.execute("http://localhost:1234", "GET", "user", "password", "entity"));
        assertNull(HttpUtils.execute("http://localhost:1234", "GET", Map.of()));
        assertNull(HttpUtils.executeGet("http://localhost:1234", "user", "password", Map.of()));
        assertNotNull(HttpUtils.executeGet("http://localhost:1234", "https://httpbin.org:443"));
        assertNotNull(HttpUtils.executeGet("http://localhost:1234", "http://httpbin.org"));
        assertNull(HttpUtils.executeDelete("http://localhost:1234", "user", "password", Map.of(), Map.of()));
    }

    @Test
    public void verifyClose() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Exception {
                HttpUtils.close(null);
                val response = mock(CloseableHttpResponse.class);
                doThrow(new RuntimeException()).when(response).close();
                HttpUtils.close(response);
            }
        });
    }

}
