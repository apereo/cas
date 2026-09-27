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
        "POST,/cas/heimdall/authorize,true",
        "POST,/cas/heimdall/authzen,true",
        "GET,/cas/heimdall/authorize,false",
        "POST,/cas/heimdall/other,false"
    })
    void verifyOperation(final String method, final String uri, final boolean supported) {
        val request = new MockHttpServletRequest(method, uri);
        assertEquals(supported, new HeimdallThrottledRequestFilter().supports(request, new MockHttpServletResponse()));
    }
}
