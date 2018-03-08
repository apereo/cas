package org.apereo.cas.trusted.web.flow;

import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

/**
 * Interface for determining a device fingerprint for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintStrategy {
    /**
     * Determine a unique browser/device fingerprint for the provided request.
     *
     * @param principal The principal uid we are generating a fingerprint for.
     * @param context   the request to generate the device fingerprint from.
     * @return The generated fingerprint
     */
    String determineFingerprint(@Nonnull String principal, @Nonnull RequestContext context);
}
