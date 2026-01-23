package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcAuditAutoConfiguration;
import org.apereo.cas.config.CasJdbcThrottlingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
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
    CasJdbcThrottlingAutoConfiguration.class,
    CasJdbcAuditAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
}, properties = {
    "cas.jdbc.show-sql=false",

    "cas.authn.throttle.core.username-parameter=username",
    "cas.authn.throttle.failure.code=AUTHENTICATION_FAILED",
    "cas.authn.throttle.core.username-parameter=username",
    "cas.authn.throttle.failure.range-seconds=5",

    "cas.authn.throttle.jdbc.user=root",
    "cas.authn.throttle.jdbc.password=password",
    "cas.authn.throttle.jdbc.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.throttle.jdbc.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.throttle.jdbc.dialect=org.hibernate.dialect.MySQLDialect",

    "cas.audit.jdbc.asynchronous=false",
    "cas.audit.jdbc.user=root",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.audit.jdbc.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
@ExtendWith(CasTestExtension.class)
@Getter
class MySQLJdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;
}
