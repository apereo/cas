package org.apereo.cas.trusted.authentication.keys;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LegacyMultifactorAuthenticationTrustRecordKeyGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 6.2.0
 */
@Deprecated(since = "6.2.0")
@Tag("Simple")
public class LegacyMultifactorAuthenticationTrustRecordKeyGeneratorTests {
    @Test
    public void verifyOperation() {
        val gen = new LegacyMultifactorAuthenticationTrustRecordKeyGenerator();
        val record = MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        assertNotNull(gen.generate(record));
    }
}
