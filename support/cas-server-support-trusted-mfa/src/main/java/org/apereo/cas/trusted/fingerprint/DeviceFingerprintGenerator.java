package org.apereo.cas.trusted.fingerprint;

import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

/**
 * Interface for generating a device fingerprint for usage within
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintGenerator {
    /**
     * Generate a unique browser/device fingerprint for the provided request.
     *
     * @param principal The principal uid we are generating a fingerprint for.
     * @param context   the request to generate the device fingerprint from.
     * @return The generated fingerprint
     */
    String generateFingerprint(@Nonnull String principal, @Nonnull RequestContext context);
}
