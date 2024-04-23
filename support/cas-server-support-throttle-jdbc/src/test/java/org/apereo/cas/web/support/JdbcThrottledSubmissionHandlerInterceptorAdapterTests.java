package org.apereo.cas.web.support;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcAuditAutoConfiguration;
import org.apereo.cas.config.CasJdbcThrottlingAutoConfiguration;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link JdbcThrottledSubmissionHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    CasJdbcThrottlingAutoConfiguration.class,
    CasJdbcAuditAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.throttle.core.username-parameter=username",
    "cas.authn.throttle.failure.code=AUTHENTICATION_FAILED",
    "cas.audit.jdbc.asynchronous=false",
    "cas.authn.throttle.core.username-parameter=username",
    "cas.authn.throttle.failure.range-seconds=5"
})
@Getter
@Tag("JDBC")
class JdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;

    @Test
    void verifyRecords() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("4.5.6.7");
        request.setRemoteUser("cas");
        request.addHeader("User-Agent", "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        throttle.recordSubmissionFailure(request);
        assertFalse(throttle.getRecords().isEmpty());
    }
}
