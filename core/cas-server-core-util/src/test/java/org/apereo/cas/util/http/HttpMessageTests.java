package org.apereo.cas.util.http;

import org.apereo.cas.web.HttpMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Francesco Cina
 * @since 4.1
 */
@Tag("Web")
class HttpMessageTests {

    @Test
    void verifyAsyncArgIsTakenIntoAccount() throws Throwable {
        assertTrue(new HttpMessage(new URI("http://www.google.com").toURL(), "messageToSend").isAsynchronous());
        assertTrue(new HttpMessage(new URI("http://www.google.com").toURL(), "messageToSend", true).isAsynchronous());
        assertFalse(new HttpMessage(new URI("http://www.google.com").toURL(), "messageToSend", false).isAsynchronous());
        assertFalse(new HttpMessage(new URI("http://www.google.com").toURL(), null, false).isAsynchronous());
    }

}

