package org.apereo.cas.trusted.fingerprint;

import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public class GeographyDeviceFingerprintGenerator implements DeviceFingerprintGenerator {
    @Override
    public String generateFingerprint(@Nonnull final HttpServletRequest request) {
        return MultifactorAuthenticationTrustUtils.generateGeography();
    }
}
