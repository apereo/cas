package org.apereo.cas.trusted.authentication.storage;

import module java.base;
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
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@ImportAutoConfiguration({
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
    private static final String GEOGRAPHY = "geography";

    private static final String DEVICE_FINGERPRINT = "deviceFingerprint";

    private String principal;

    private String otherPrincipal;

    @BeforeEach
    void initialize() {
        principal = UUID.randomUUID().toString();
        otherPrincipal = UUID.randomUUID().toString();
    }

    @Test
    void verifyExpireByKey() {
        var record = MultifactorAuthenticationTrustRecord.newInstance(principal, GEOGRAPHY, DEVICE_FINGERPRINT);
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));
        
        record = MultifactorAuthenticationTrustRecord.newInstance(principal, GEOGRAPHY, DEVICE_FINGERPRINT);
        record = getMfaTrustEngine().save(record);
        assertNotNull(getMfaTrustEngine().get(record.getId()));

        val records = getMfaTrustEngine().get(principal);
        assertEquals(2, records.size());

        getMfaTrustEngine().remove(records.stream().findFirst().orElseThrow().getRecordKey());
        assertEquals(1, getMfaTrustEngine().get(principal).size());
        assertTrue(getMfaTrustEngine().isAvailable());
    }

    @Test
    void verifyRetrieveAndExpireByDate() {
        val now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        Stream.of(principal, otherPrincipal).forEach(p -> {
            for (var offset = 0; offset < 3; offset++) {
                val record = MultifactorAuthenticationTrustRecord.newInstance(p, GEOGRAPHY, DEVICE_FINGERPRINT);
                record.setRecordDate(now.minusDays(offset));
                record.setExpirationDate(DateTimeUtils.dateOf(now.plusDays(1)));
                getMfaTrustEngine().save(record);
            }
        });
        Stream.of(principal, otherPrincipal).forEach(p -> {
            assertEquals(3, getMfaTrustEngine().get(p, now.minusDays(30)).size());
            assertEquals(1, getMfaTrustEngine().get(p, now.minusSeconds(1)).size());
        });
    }

    @Test
    void verifyStoreAndRetrieve() {
        val original = MultifactorAuthenticationTrustRecord.newInstance(principal, GEOGRAPHY, DEVICE_FINGERPRINT);
        getMfaTrustEngine().save(original);
        val records = getMfaTrustEngine().get(principal);
        assertEquals(1, records.size());
        val record = records.stream().findFirst().orElseThrow();

        assertEquals(keyGenerationStrategy.generate(original), keyGenerationStrategy.generate(record));
    }

    @AfterEach
    public void emptyTrustEngine() {
        Stream.of(principal, otherPrincipal)
            .map(getMfaTrustEngine()::get)
            .flatMap(Set::stream)
            .forEach(r -> getMfaTrustEngine().remove(r.getRecordKey()));

        assertTrue(getMfaTrustEngine().get(principal).isEmpty());
        assertTrue(getMfaTrustEngine().get(otherPrincipal).isEmpty());
    }
}
