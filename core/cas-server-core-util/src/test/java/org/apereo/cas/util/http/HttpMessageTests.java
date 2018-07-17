package org.apereo.cas.util.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Francesco Cina
 * @since 4.1
 */
@Slf4j
public class HttpMessageTests {

    @Test
    public void verifyAsyncArgIsTakenIntoAccount() throws Exception {
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend").isAsynchronous());
        assertTrue(new HttpMessage(new URL("http://www.google.com"), "messageToSend", true).isAsynchronous());
        assertFalse(new HttpMessage(new URL("http://www.google.com"), "messageToSend", false).isAsynchronous());
    }

}

