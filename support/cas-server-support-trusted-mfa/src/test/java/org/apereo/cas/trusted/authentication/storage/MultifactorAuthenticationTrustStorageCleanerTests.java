package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class MultifactorAuthenticationTrustStorageCleanerTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyAction() {
        try {
            final var record = getMultifactorAuthenticationTrustRecord();
            record.setRecordDate(LocalDateTime.now().minusDays(1));
            mfaTrustEngine.set(record);
            mfaTrustStorageCleaner.clean();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
