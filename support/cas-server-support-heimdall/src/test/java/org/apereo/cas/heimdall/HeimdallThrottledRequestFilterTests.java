package org.apereo.cas.heimdall;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HeimdallThrottledRequestFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class HeimdallThrottledRequestFilterTests {

    @ParameterizedTest
    @CsvSource({
        "POST,/cas/heimdall/authorize,200,true",
        "POST,/cas/heimdall/authorize,401,true",
        "POST,/cas/heimdall/authorize,403,false",
        "POST,/cas/heimdall/authorize,404,false",
        "POST,/cas/heimdall/authorize,400,false",
        "GET,/cas/heimdall/authorize,200,false",
        "POST,/cas/heimdall/authzen,200,true",
        "POST,/cas/heimdall/authzen,401,true",
        "POST,/cas/heimdall/authzen,400,false",
        "POST,/cas/heimdall/authzen,500,false",
        "POST,/cas/heimdall/other,401,false"
    })
    void verifyOperation(final String method, final String uri, final int status, final boolean supported) {
        val request = new MockHttpServletRequest(method, uri);
        val response = new MockHttpServletResponse();
        response.setStatus(status);
        assertEquals(supported, new HeimdallThrottledRequestFilter().supports(request, response));
    }
}
