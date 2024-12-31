package org.apereo.cas.web;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.config.CasBucket4jThrottlingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseBucket4jThrottledRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasBucket4jThrottlingAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseBucket4jThrottledRequestTests {
    @Autowired
    @Qualifier(ThrottledRequestExecutor.DEFAULT_BEAN_NAME)
    protected ThrottledRequestExecutor throttledRequestExecutor;

    @BeforeEach
    void onSetUp() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.100");
        request.setLocalAddr("223.456.789.200");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyOperation() {
        assertNotNull(throttledRequestExecutor);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        assertFalse(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));

        assertFalse(throttledRequestExecutor.throttle(request, response));
        assertTrue(response.containsHeader(BucketConsumer.HEADER_NAME_X_RATE_LIMIT_REMAINING));
    }
}

