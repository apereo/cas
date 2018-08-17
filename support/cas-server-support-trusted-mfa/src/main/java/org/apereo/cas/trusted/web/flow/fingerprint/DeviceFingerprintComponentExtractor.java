package org.apereo.cas.trusted.web.flow.fingerprint;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * Interface for extracting a device fingerprint component for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintComponentExtractor extends Ordered {
    /**
     * Return a no-op DeviceFingerprintComponent.
     *
     * @return a no-op DeviceFingerprintComponent.
     */
    static DeviceFingerprintComponentExtractor noOp() {
        return (principal, context, isNew) -> Optional.empty();
    }

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * Determine a unique browser/device fingerprint component for the provided request.
     *
     * @param principal The principal uid we are generating a fingerprint for.
     * @param context   the request to generate the device fingerprint from.
     * @param isNew     a boolean indicating if we are currently recording a new trusted device
     * @return The fingerprint component
     */
    Optional<String> extractComponent(String principal, RequestContext context, boolean isNew);
}
