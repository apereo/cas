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
     * @param context the request to generate the device fingerprint from.
     * @return The generated fingerprint
     */
    String generateFingerprint(@Nonnull RequestContext context);
}
