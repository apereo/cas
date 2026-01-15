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

    "cas.authn.throttle.failure.code=AUTHENTICATION_FAILED",
    "cas.authn.throttle.core.username-parameter=username",
    "cas.authn.throttle.failure.range-seconds=5",

    "cas.authn.throttle.jdbc.user=postgres",
    "cas.authn.throttle.jdbc.password=password",
    "cas.authn.throttle.jdbc.driver-class=org.postgresql.Driver",
    "cas.authn.throttle.jdbc.url=jdbc:postgresql://localhost:5432/audit",
    "cas.authn.throttle.jdbc.dialect=org.hibernate.dialect.PostgreSQLDialect",

    "cas.audit.jdbc.asynchronous=false",
    "cas.audit.jdbc.user=postgres",
    "cas.audit.jdbc.password=password",
    "cas.audit.jdbc.driver-class=org.postgresql.Driver",
    "cas.audit.jdbc.url=jdbc:postgresql://localhost:5432/audit",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@ExtendWith(CasTestExtension.class)
@Getter
class PostgresJdbcThrottledSubmissionHandlerInterceptorAdapterTests extends BaseThrottledSubmissionHandlerInterceptorAdapterTests {
    @Autowired
    @Qualifier(ThrottledSubmissionHandlerInterceptor.BEAN_NAME)
    private ThrottledSubmissionHandlerInterceptor throttle;
}
