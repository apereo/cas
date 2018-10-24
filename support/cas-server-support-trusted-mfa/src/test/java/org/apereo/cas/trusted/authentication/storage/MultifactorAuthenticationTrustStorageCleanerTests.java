package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class MultifactorAuthenticationTrustStorageCleanerTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Autowired
    @Qualifier("mfaTrustEngine")
    protected MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Test
    public void verifyAction() {
        try {
            val record = getMultifactorAuthenticationTrustRecord();
            record.setRecordDate(LocalDateTime.now().minusDays(1));
            getMfaTrustEngine().set(record);
            mfaTrustStorageCleaner.clean();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
