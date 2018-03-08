package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

/**
 * Default {@link DeviceFingerprintStrategy} implementation that uses request geography for fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class GeographyDeviceFingerprintStrategy implements DeviceFingerprintStrategy {
    @Override
    public String determineFingerprint(@Nonnull final String principal, @Nonnull final RequestContext context) {
        return MultifactorAuthenticationTrustUtils.generateGeography();
    }
}
