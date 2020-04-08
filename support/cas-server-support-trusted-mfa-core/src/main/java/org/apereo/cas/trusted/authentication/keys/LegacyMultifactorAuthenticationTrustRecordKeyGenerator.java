package org.apereo.cas.trusted.authentication.keys;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link LegacyMultifactorAuthenticationTrustRecordKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 6.2.0
 */
@Slf4j
@Deprecated(since = "6.2.0")
public class LegacyMultifactorAuthenticationTrustRecordKeyGenerator
    implements MultifactorAuthenticationTrustRecordKeyGenerator {
    @Override
    public String generate(final MultifactorAuthenticationTrustRecord record) {
        val result = record.getPrincipal()
            + '@'
            + record.getRecordDate()
            + '@'
            + record.getDeviceFingerprint();
        LOGGER.trace("Generated multifactor trusted record key [{}]", result);
        return result;
    }
}
