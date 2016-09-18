package org.apereo.cas.util.http;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

/**
 *
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

