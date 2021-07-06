package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustStorageCleanerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("MFATrustedDevices")
public class MultifactorAuthenticationTrustStorageCleanerTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @Autowired
    @Qualifier("mfaTrustStorageCleaner")
    protected MultifactorAuthenticationTrustStorageCleaner mfaTrustStorageCleaner;

    @Test
    public void verifyAction() {
        assertNotNull(mfaTrustStorageCleaner.getStorage());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val record = getMultifactorAuthenticationTrustRecord();
                record.setRecordDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));
                getMfaTrustEngine().save(record);
                mfaTrustStorageCleaner.clean();
            }
        });
    }
}
