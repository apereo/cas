package org.apereo.cas.trusted.authentication.keys;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link DefaultMultifactorAuthenticationTrustRecordKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class DefaultMultifactorAuthenticationTrustRecordKeyGenerator
    implements MultifactorAuthenticationTrustRecordKeyGenerator {
    @Override
    public String generate(final MultifactorAuthenticationTrustRecord record) {
        val result = record.getPrincipal()
            + '@'
            + record.getName()
            + '@'
            + record.getDeviceFingerprint();
        LOGGER.trace("Generated multifactor trusted record key [{}]", result);
        return result;
    }
}
