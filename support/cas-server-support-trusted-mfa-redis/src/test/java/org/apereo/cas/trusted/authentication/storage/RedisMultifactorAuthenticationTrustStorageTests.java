package org.apereo.cas.trusted.authentication.storage;


import org.apereo.cas.config.CasRedisMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisMultifactorAuthenticationTrustStorageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Redis")
@ImportAutoConfiguration(CasRedisMultifactorAuthenticationTrustAutoConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.authn.mfa.trusted.redis.host=localhost",
        "cas.authn.mfa.trusted.redis.port=6379"
    })
@EnabledIfListeningOnPort(port = 6379)
@Getter
class RedisMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("redisMfaTrustedAuthnTemplate")
    private CasRedisTemplate<String, List<MultifactorAuthenticationTrustRecord>> redisMfaTrustedAuthnTemplate;

    @BeforeEach
    void setup() {
        val key = RedisMultifactorAuthenticationTrustStorage.CAS_PREFIX + '*';
        try (val keys = redisMfaTrustedAuthnTemplate.scan(key, 0L)) {
            redisMfaTrustedAuthnTemplate.delete(keys.collect(Collectors.toSet()));
        }
    }

    @Test
    void verifySetAnExpireByKey() {
        val user = UUID.randomUUID().toString();
        var record = MultifactorAuthenticationTrustRecord.newInstance(user, "geography", "fingerprint");
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));
        
        val records = getMfaTrustEngine().get(user);
        assertEquals(1, records.size());
        getMfaTrustEngine().remove(records.stream().findFirst().get().getRecordKey());
        assertTrue(getMfaTrustEngine().get(user).isEmpty());
    }

    @Test
    void verifyMultipleDevicesPerUser() {
        val user = UUID.randomUUID().toString();
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(user, "geography", "fingerprint"));
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(user, "geography bis", "fingerprint bis"));
        getMfaTrustEngine().save(MultifactorAuthenticationTrustRecord.newInstance(UUID.randomUUID().toString(), "geography2", "fingerprint2"));
        val records = getMfaTrustEngine().get(user);
        assertEquals(2, records.size());
    }


    @Test
    void verifyExpireByDate() {
        val user = UUID.randomUUID().toString();
        val record = MultifactorAuthenticationTrustRecord.newInstance(user, "geography", "fingerprint");
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        record.setRecordDate(now.minusDays(2));
        getMfaTrustEngine().save(record);
        assertEquals(1, getMfaTrustEngine().get(now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(now.minusDays(1)).size());
    }

    @BeforeEach
    void emptyTrustEngine() {
        getMfaTrustEngine().getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }
}
