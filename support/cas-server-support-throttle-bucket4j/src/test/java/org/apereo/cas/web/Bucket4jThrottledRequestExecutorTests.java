package org.apereo.cas.web;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link Bucket4jThrottledRequestExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationThrottling")
public class Bucket4jThrottledRequestExecutorTests {

    @TestPropertySource(properties = {
        "cas.authn.throttle.bucket4j.bandwidth[0].capacity=2",
        "cas.authn.throttle.bucket4j.blocking=true"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class BlockingTests extends BaseBucket4jThrottledRequestTests {
    }

    @TestPropertySource(properties = {
        "cas.authn.throttle.bucket4j.bandwidth[0].capacity=1",
        "cas.authn.throttle.bucket4j.blocking=true"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class NonBlockingTests extends BaseBucket4jThrottledRequestTests {
    }
}
