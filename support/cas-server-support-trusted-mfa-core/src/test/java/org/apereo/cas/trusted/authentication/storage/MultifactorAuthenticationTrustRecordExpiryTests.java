package org.apereo.cas.trusted.authentication.storage;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustRecordExpiryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Tag("MFATrustedDevices")
@ExtendWith(CasTestExtension.class)
class MultifactorAuthenticationTrustRecordExpiryTests {
    @Test
    void verifyOperation() {
        val expiry = new MultifactorAuthenticationTrustRecordExpiry();

        val record = new MultifactorAuthenticationTrustRecord();
        record.setDeviceFingerprint(UUID.randomUUID().toString());
        record.setName("DeviceName");
        record.setPrincipal(UUID.randomUUID().toString());
        record.setId(1000);
        record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
        record.setRecordKey(UUID.randomUUID().toString());

        assertEquals(Long.MAX_VALUE, expiry.expireAfterUpdate(record.getRecordKey(),
            record, System.currentTimeMillis(), 1000));

        record.setExpirationDate(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1)));
        assertEquals(0, expiry.expireAfterUpdate(record.getRecordKey(),
            record, System.currentTimeMillis(), 1000));

        record.setExpirationDate(DateTimeUtils.dateOf(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1)));
        assertTrue(expiry.expireAfterRead(record.getRecordKey(),
            record, System.currentTimeMillis(), 1000) > 0);
    }
}
