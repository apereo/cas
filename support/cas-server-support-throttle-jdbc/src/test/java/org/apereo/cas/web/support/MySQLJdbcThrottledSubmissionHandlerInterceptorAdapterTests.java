package org.apereo.cas.web.support;

import org.apereo.cas.audit.config.CasSupportJdbcAuditConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasJdbcThrottlingConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
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
    "cas.jdbc.showSql=true",

    "cas.authn.throttle.usernameParameter=username",
    "cas.authn.throttle.failure.code=AUTHENTICATION_FAILED",
    "cas.authn.throttle.usernameParameter=username",
    "cas.authn.throttle.failure.range-seconds=5",

    "cas.authn.throttle.jdbc.user=root",
    "cas.authn.throttle.jdbc.password=password",
    "cas.authn.throttle.jdbc.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.throttle.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.throttle.jdbc.dialect=org.hibernate.dialect.MySQL57InnoDBDialect",

    "cas.audit.jdbc.asynchronous=false",
    "cas.audit.jdbc.user=root",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.audit.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
@Getter
public class MySQLJdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;
}
