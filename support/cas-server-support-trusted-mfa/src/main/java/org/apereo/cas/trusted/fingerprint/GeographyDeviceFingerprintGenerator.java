package org.apereo.cas.trusted.fingerprint;

import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

/**
 * Default {@link DeviceFingerprintGenerator} implementation that uses request geography for fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class GeographyDeviceFingerprintGenerator implements DeviceFingerprintGenerator {
    @Override
    public String generateFingerprint(@Nonnull final String principal, @Nonnull final RequestContext context) {
        return MultifactorAuthenticationTrustUtils.generateGeography();
    }
}
