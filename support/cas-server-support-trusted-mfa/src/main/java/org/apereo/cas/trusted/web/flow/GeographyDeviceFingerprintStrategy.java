package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * Default {@link DeviceFingerprintStrategy} implementation that uses request geography for fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public class GeographyDeviceFingerprintStrategy implements DeviceFingerprintStrategy {
    @Override
    public String determineFingerprint(final String principal, final RequestContext context) {
        return MultifactorAuthenticationTrustUtils.generateGeography();
    }
}
