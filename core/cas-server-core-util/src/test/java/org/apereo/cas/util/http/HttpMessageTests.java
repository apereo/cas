package org.apereo.cas.util.http;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Francesco Cina
 * @since 4.1
 */
@Tag("Simple")
public class HttpMessageTests {

    @Test
    @SneakyThrows
    public void verifyAsyncArgIsTakenIntoAccount() {
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend").isAsynchronous());
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend", true).isAsynchronous());
        assertFalse(new HttpMessage(new URL("http://www.google.com"), "messageToSend", false).isAsynchronous());
    }

}

