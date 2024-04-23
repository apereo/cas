package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Import({
    CasJdbcMultifactorAuthnTrustAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class
})
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Tag("JDBCMFA")
@Getter
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.mfa.trusted.jpa.ddl-auto=create-drop",
    "cas.authn.mfa.trusted.cleaner.schedule.enabled=false",
    "cas.jdbc.physical-table-names.JpaMultifactorAuthenticationTrustRecord=mfaauthntrustedrec"
})
class JpaMultifactorAuthenticationTrustStorageTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    private static final String PRINCIPAL = "principal";

    private static final String PRINCIPAL2 = "principal2";

    private static final String GEOGRAPHY = "geography";

    private static final String DEVICE_FINGERPRINT = "deviceFingerprint";

    @BeforeEach
    public void clearEngine() {
        getMfaTrustEngine().getAll().forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));
    }

    @Test
    void verifyExpireByKey() throws Throwable {
        var record = MultifactorAuthenticationTrustRecord.newInstance(PRINCIPAL, GEOGRAPHY, DEVICE_FINGERPRINT);
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));
        
        record = MultifactorAuthenticationTrustRecord.newInstance(PRINCIPAL, GEOGRAPHY, DEVICE_FINGERPRINT);
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));

        val records = getMfaTrustEngine().get(PRINCIPAL);
        assertEquals(2, records.size());

        getMfaTrustEngine().remove(records.stream().findFirst().orElseThrow().getRecordKey());
        assertEquals(1, getMfaTrustEngine().get(PRINCIPAL).size());
        assertTrue(getMfaTrustEngine().isAvailable());
    }

    @Test
    void verifyRetrieveAndExpireByDate() throws Throwable {
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        Stream.of(PRINCIPAL, PRINCIPAL2).forEach(p -> {
            for (var offset = 0; offset < 3; offset++) {
                val record = MultifactorAuthenticationTrustRecord.newInstance(p, GEOGRAPHY, DEVICE_FINGERPRINT);
                record.setRecordDate(now.minusDays(offset));
                record.setExpirationDate(DateTimeUtils.dateOf(now.plusDays(1)));
                getMfaTrustEngine().save(record);
            }
        });
        assertEquals(6, getMfaTrustEngine().get(now.minusDays(30)).size());
        assertEquals(2, getMfaTrustEngine().get(now.minusSeconds(1)).size());

        getMfaTrustEngine().remove(now.plusDays(10));
        assertTrue(getMfaTrustEngine().getAll().isEmpty());
    }

    @Test
    void verifyStoreAndRetrieve() throws Throwable {
        val original = MultifactorAuthenticationTrustRecord.newInstance(PRINCIPAL, GEOGRAPHY, DEVICE_FINGERPRINT);
        getMfaTrustEngine().save(original);
        val records = getMfaTrustEngine().get(PRINCIPAL);
        assertEquals(1, records.size());
        val record = records.stream().findFirst().orElseThrow();

        assertEquals(keyGenerationStrategy.generate(original), keyGenerationStrategy.generate(record));
    }

    @AfterEach
    public void emptyTrustEngine() {
        Stream.of(PRINCIPAL, PRINCIPAL2)
            .map(getMfaTrustEngine()::get)
            .flatMap(Set::stream)
            .forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));

        assertTrue(getMfaTrustEngine().get(PRINCIPAL).isEmpty());
        assertTrue(getMfaTrustEngine().get(PRINCIPAL2).isEmpty());
    }
}
