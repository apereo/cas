package org.apereo.cas.web;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.config.CasBucket4jThrottlingConfiguration;
import org.apereo.cas.throttle.ThrottledRequestExecutor;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseBucket4jThrottledRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasBucket4jThrottlingConfiguration.class
})
public abstract class BaseBucket4jThrottledRequestTests {
    @Autowired
    @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    protected ThrottledRequestExecutor throttledRequestExecutor;

    @Test
    public void verifyOperation() {
        assertNotNull(throttledRequestExecutor);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertTrue(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));

        assertTrue(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));
    }
}

