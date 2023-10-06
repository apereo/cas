package org.apereo.cas.web;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.config.CasBucket4jThrottlingConfiguration;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    CasBucket4jThrottlingConfiguration.class
})
public abstract class BaseBucket4jThrottledRequestTests {
    @Autowired
    @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    protected ThrottledRequestExecutor throttledRequestExecutor;

    @BeforeEach
    public void onSetUp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.100");
        request.setLocalAddr("223.456.789.200");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(throttledRequestExecutor);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));

        assertFalse(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));
    }
}

