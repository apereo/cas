package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.BaseAuditConfigurationTests;
import org.apereo.cas.config.CasSupportRedisAuditAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    BaseAuditConfigurationTests.SharedTestConfiguration.class,
    CasSupportRedisAuditAutoConfiguration.class
},
    properties = {
        "cas.audit.redis.host=localhost",
        "cas.audit.redis.port=6379",
        "cas.audit.redis.max-age=P1D",
        "cas.audit.redis.asynchronous=false"
    })
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 6379)
class RedisAuditTrailManagerTests extends BaseAuditConfigurationTests {
    @Autowired
    @Qualifier("redisAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired
    @Qualifier("auditRedisTemplate")
    private CasRedisTemplate auditRedisTemplate;

    private static AuditActionContext newAuditRecord(final String principal, final LocalDateTime when) {
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London");
        return new AuditActionContext(principal, "TEST", "TEST", "CAS", when, clientInfo);
    }

    @Test
    void verifyRecordsAtSameInstantArePreserved() {
        val principal = RandomUtils.randomAlphanumeric(12);
        val when = LocalDateTime.now(Clock.systemUTC());
        val actions = Set.of("AUTHENTICATION_SUCCESS", "TICKET_GRANTING_TICKET_CREATED", "SERVICE_TICKET_CREATED");
        try {
            for (val action : actions) {
                val record = new AuditActionContext(principal, "TEST", action, "CAS", when,
                    new ClientInfo("127.0.0.1", "127.0.0.1", "MCP", "unknown"));
                auditTrailManager.record(record);
            }
            val results = auditTrailManager.getAuditRecords(Map.of(
                AuditTrailManager.WhereClauseFields.DATE, when.minusSeconds(1),
                AuditTrailManager.WhereClauseFields.PRINCIPAL, principal));
            assertEquals(actions.size(), results.size());
            assertEquals(actions, results.stream().map(AuditActionContext::getActionPerformed).collect(Collectors.toSet()));
        } finally {
            try (val keys = auditRedisTemplate.scan(RedisAuditTrailManager.CAS_AUDIT_CONTEXT_PREFIX + "*:" + principal)) {
                keys.forEach(auditRedisTemplate::delete);
            }
        }
    }

    @Test
    void verifyLegacyRecordsRemainReadable() {
        val principal = RandomUtils.randomAlphanumeric(12);
        val when = LocalDateTime.now(Clock.systemUTC());
        val legacyKey = RedisAuditTrailManager.CAS_AUDIT_CONTEXT_PREFIX + when.toEpochSecond(ZoneOffset.UTC) + ':' + principal;
        try {
            auditRedisTemplate.opsForValue().set(legacyKey, newAuditRecord(principal, when));
            auditTrailManager.record(newAuditRecord(principal, when));
            val results = auditTrailManager.getAuditRecords(Map.of(
                AuditTrailManager.WhereClauseFields.DATE, when.minusSeconds(1),
                AuditTrailManager.WhereClauseFields.PRINCIPAL, principal));
            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(record -> principal.equals(record.getPrincipal())));
        } finally {
            try (val keys = auditRedisTemplate.scan(RedisAuditTrailManager.CAS_AUDIT_CONTEXT_PREFIX + "*:" + principal)) {
                keys.forEach(auditRedisTemplate::delete);
            }
        }
    }

    @Test
    void verifyRecordsExpire() {
        val principal = RandomUtils.randomAlphanumeric(12);
        auditTrailManager.record(newAuditRecord(principal, LocalDateTime.now(Clock.systemUTC())));
        try (val keys = auditRedisTemplate.scan(RedisAuditTrailManager.CAS_AUDIT_CONTEXT_PREFIX + "*:" + principal)) {
            val key = keys.findFirst().orElseThrow();
            val ttl = auditRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            assertTrue(ttl > 0 && ttl <= Duration.ofDays(1).toSeconds());
        }
    }

    @Test
    void verifyNewestRecordsByPrincipal() {
        val principal = RandomUtils.randomAlphanumeric(12);
        val now = LocalDateTime.now(Clock.systemUTC());
        auditTrailManager.record(newAuditRecord(principal, now.minusSeconds(3)));
        auditTrailManager.record(newAuditRecord(principal, now.minusSeconds(2)));
        auditTrailManager.record(newAuditRecord(principal, now.minusSeconds(1)));
        auditTrailManager.record(newAuditRecord(principal, now.minusDays(3)));

        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, now.minusDays(1),
            AuditTrailManager.WhereClauseFields.COUNT, 2L,
            AuditTrailManager.WhereClauseFields.PRINCIPAL, principal);
        val results = auditTrailManager.getAuditRecords(criteria);
        assertEquals(2, results.size());
        assertEquals(now.minusSeconds(1).toEpochSecond(ZoneOffset.UTC),
            results.getFirst().getWhenActionWasPerformed().toEpochSecond(ZoneOffset.UTC));
        assertEquals(now.minusSeconds(2).toEpochSecond(ZoneOffset.UTC),
            results.getLast().getWhenActionWasPerformed().toEpochSecond(ZoneOffset.UTC));
    }

    @Test
    void verifyPrincipalIsNotTreatedAsPattern() {
        val prefix = RandomUtils.randomAlphanumeric(12);
        val now = LocalDateTime.now(Clock.systemUTC());
        auditTrailManager.record(newAuditRecord(prefix + '*', now));
        auditTrailManager.record(newAuditRecord(prefix + "-other", now));

        val criteria = Map.<AuditTrailManager.WhereClauseFields, Object>of(
            AuditTrailManager.WhereClauseFields.DATE, now.minusDays(1),
            AuditTrailManager.WhereClauseFields.PRINCIPAL, prefix + '*');
        val results = auditTrailManager.getAuditRecords(criteria);
        assertEquals(1, results.size());
        assertEquals(prefix + '*', results.getFirst().getPrincipal());
    }
}
