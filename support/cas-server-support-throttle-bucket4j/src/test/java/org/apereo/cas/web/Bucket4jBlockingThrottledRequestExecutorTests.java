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
 * This is {@link Bucket4jBlockingThrottledRequestExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasBucket4jThrottlingConfiguration.class
}, properties = {
    "cas.authn.throttle.bucket4j.overdraft=1",
    "cas.authn.throttle.bucket4j.capacity=1",
    "cas.authn.throttle.bucket4j.blocking=false"
})
@Tag("AuthenticationThrottling")
public class Bucket4jBlockingThrottledRequestExecutorTests {
    @Autowired
    @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    private ThrottledRequestExecutor throttledRequestExecutor;

    @Test
    public void verifyOperation() {
        assertNotNull(throttledRequestExecutor);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(this.throttledRequestExecutor.throttle(request, response));
        assertNotNull(response.getHeader(Bucket4jThrottledRequestExecutor.HEADER_NAME_X_RATE_LIMIT_REMAINING));

        assertTrue(this.throttledRequestExecutor.throttle(request, response));
        assertNotNull(response.getHeader(Bucket4jThrottledRequestExecutor.HEADER_NAME_X_RATE_LIMIT_RETRY_AFTER_SECONDS));
    }
}
