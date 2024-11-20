package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.*;

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
    void verifyOperation() throws Exception {
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
