package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasSupportRedisAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link RedisAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasCoreAuditConfiguration.class,
    CasSupportRedisAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class},
    properties = {
        "cas.audit.redis.host=localhost",
        "cas.audit.redis.port=6379",
        "cas.audit.redis.asynchronous=false"
    })
@Tag("Redis")
@Getter
@EnabledIfPortOpen(port = 6379)
public class RedisAuditTrailManagerTests extends BaseAuditConfigurationTests {
    @Autowired
    @Qualifier("redisAuditTrailManager")
    private AuditTrailManager auditTrailManager;
}
