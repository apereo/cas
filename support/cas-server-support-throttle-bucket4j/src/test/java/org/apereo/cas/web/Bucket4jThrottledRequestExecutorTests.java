package org.apereo.cas.web;

import org.apereo.cas.config.CasBucket4jThrottlingConfiguration;
import org.apereo.cas.throttle.ThrottledRequestExecutor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link Bucket4jThrottledRequestExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasBucket4jThrottlingConfiguration.class
})
@Tag("AuthenticationThrottling")
public class Bucket4jThrottledRequestExecutorTests {
    @Autowired
    @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    private ThrottledRequestExecutor throttledRequestExecutor;

    @Test
    public void verifyOperation() {
        assertNotNull(throttledRequestExecutor);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(this.throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(Bucket4jThrottledRequestExecutor.HEADER_NAME_X_RATE_LIMIT_REMAINING));
    }
}
