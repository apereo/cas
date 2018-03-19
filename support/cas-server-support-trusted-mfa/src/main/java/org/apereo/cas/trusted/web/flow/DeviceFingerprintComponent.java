package org.apereo.cas.trusted.web.flow;

import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Interface for determining a device fingerprint component for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintComponent extends Ordered {
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
    @Nonnull
    Optional<String> determineComponent(@Nonnull String principal, @Nonnull RequestContext context, boolean isNew);
}
