package org.apereo.cas.web.support;

import org.apereo.cas.audit.config.CasSupportJdbcAuditConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasJdbcThrottlingConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit test for {@link JdbcThrottledSubmissionHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    CasJdbcThrottlingConfiguration.class,
    CasSupportJdbcAuditConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.throttle.usernameParameter=username",
    "cas.authn.throttle.failure.code=AUTHENTICATION_FAILED",
    "cas.audit.jdbc.asynchronous=false",
    "cas.authn.throttle.usernameParameter=username",
    "cas.authn.throttle.failure.range-seconds=5"
})
@Getter
@Tag("JDBC")
public class JdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;
}
