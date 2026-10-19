package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.config.CasRedisMultifactorAuthenticationTrustAutoConfiguration;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.keys.DefaultMultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void verifyUnavailableRedis() {
        assertTrue(getMfaTrustEngine().isAvailable());
        val redisTemplate = mock(CasRedisTemplate.class);
        when(redisTemplate.execute(any(RedisCallback.class))).thenThrow(new RedisConnectionFailureException("down"));
        when(redisTemplate.scan(anyString())).thenThrow(new RedisConnectionFailureException("down"));
        val storage = new RedisMultifactorAuthenticationTrustStorage(new TrustedDevicesMultifactorProperties(),
            CipherExecutor.noOpOfSerializableToString(), redisTemplate, new DefaultMultifactorAuthenticationTrustRecordKeyGenerator());
        assertFalse(storage.isAvailable());
        assertTrue(storage.get("casuser").isEmpty());
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
        assertEquals(1, getMfaTrustEngine().get(user, now.minusDays(30)).size());
        assertEquals(0, getMfaTrustEngine().get(user, now.minusDays(1)).size());
    }
}
