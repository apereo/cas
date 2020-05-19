package org.apereo.cas.throttle;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultThrottledRequestResponseHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultThrottledRequestResponseHandlerTests {

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addParameter("username", "casuser");
        val handler = new DefaultThrottledRequestResponseHandler("username");
        val response = new MockHttpServletResponse();
        assertFalse(handler.handle(request, response));
        assertEquals(HttpStatus.SC_LOCKED, response.getStatus());
    }
}
