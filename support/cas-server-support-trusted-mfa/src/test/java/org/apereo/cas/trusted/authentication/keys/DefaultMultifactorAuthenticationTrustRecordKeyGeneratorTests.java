package org.apereo.cas.trusted.authentication.keys;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMultifactorAuthenticationTrustRecordKeyGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DefaultMultifactorAuthenticationTrustRecordKeyGeneratorTests {
    @Test
    public void verifyOperation() {
        val gen = new DefaultMultifactorAuthenticationTrustRecordKeyGenerator();
        val record = MultifactorAuthenticationTrustRecord.newInstance("casuser", "geography", "fingerprint");
        assertNotNull(gen.generate(record));
    }
}
