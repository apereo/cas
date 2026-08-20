package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcAuditAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is {@link CasJdbcAuditScheduleEnabledOnHostConfigurationTests}.
 *
 * @since 8.0.0
 */
@SpringBootTest(
    classes = {
        BaseAuditConfigurationTests.SharedTestConfiguration.class,
        CasJdbcAuditAutoConfiguration.class,
        CasHibernateJpaAutoConfiguration.class
    },
    properties = {
        "cas.jdbc.show-sql=true",
        "cas.audit.jdbc.column-length=-1",
        "cas.audit.jdbc.asynchronous=false",
        "cas.audit.jdbc.schedule.enabled=true",
        "cas.audit.jdbc.schedule.enabled-on-host=some-host-that-does-not-exist-anywhere"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("JDBC")
@ExtendWith(CasTestExtension.class)
class CasJdbcAuditScheduleEnabledOnHostConfigurationTests {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The {@code enabled-on-host} property is set to a hostname that can never match
     * the local machine, so the {@code inspektrAuditTrailCleaner} bean must be skipped
     * entirely rather than registered and simply left un-scheduled.
     */
    @Test
    void verifyCleanerIsNotRegisteredOnNonMatchingHost() {
        assertThat(applicationContext.containsBean("inspektrAuditTrailCleaner")).isFalse();
    }
}
