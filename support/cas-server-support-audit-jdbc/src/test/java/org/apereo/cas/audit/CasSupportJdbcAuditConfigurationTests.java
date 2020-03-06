package org.apereo.cas.audit;

import org.apereo.cas.audit.config.CasSupportJdbcAuditConfiguration;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CasSupportJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportJdbcAuditConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreUtilConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = "cas.audit.jdbc.asynchronous=false")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Getter
@Tag("JDBC")
public class CasSupportJdbcAuditConfigurationTests extends BaseAuditConfigurationTests {

    @Autowired
    @Qualifier("jdbcAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
