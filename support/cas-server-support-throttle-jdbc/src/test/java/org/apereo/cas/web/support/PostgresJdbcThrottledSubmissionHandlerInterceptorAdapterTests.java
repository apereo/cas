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

    "cas.authn.throttle.jdbc.user=postgres",
    "cas.authn.throttle.jdbc.password=password",
    "cas.authn.throttle.jdbc.driverClass=org.postgresql.Driver",
    "cas.authn.throttle.jdbc.url=jdbc:postgresql://localhost:5432/audit",
    "cas.authn.throttle.jdbc.dialect=org.hibernate.dialect.PostgreSQL95Dialect",

    "cas.audit.jdbc.asynchronous=false",
    "cas.audit.jdbc.user=postgres",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driverClass=org.postgresql.Driver",
    "cas.audit.jdbc.url=jdbc:postgresql://localhost:5432/audit",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
@Getter
public class PostgresJdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    @Autowired
    @Qualifier("authenticationThrottle")
    private ThrottledSubmissionHandlerInterceptor throttle;
}
