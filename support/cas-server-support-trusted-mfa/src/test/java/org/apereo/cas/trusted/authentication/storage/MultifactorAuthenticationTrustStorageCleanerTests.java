package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("MFA")
public class MultifactorAuthenticationTrustStorageCleanerTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Test
    public void verifyAction() {
        try {
            val record = getMultifactorAuthenticationTrustRecord();
            record.setRecordDate(LocalDateTime.now(ZoneId.systemDefault()).minusDays(1));
            getMfaTrustEngine().save(record);
            mfaTrustStorageCleaner.clean();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
