package org.apereo.cas.util.http;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Francesco Cina
 * @since 4.1
 */
public class HttpMessageTests {

    @Test
    public void verifyAsyncArgIsTakenIntoAccount() throws Exception {
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend").isAsynchronous());
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend", true).isAsynchronous());
        assertFalse(new HttpMessage(new URL("http://www.google.com"), "messageToSend", false).isAsynchronous());
    }

}

