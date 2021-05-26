package org.apereo.cas.trusted.web.flow.fingerprint;

import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Interface for extracting a device fingerprint component for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintComponentManager extends Ordered {
    /**
     * Return a no-op DeviceFingerprintComponent.
     *
     * @return a no-op DeviceFingerprintComponent.
     */
    static DeviceFingerprintComponentManager noOp() {
        return (principal, request, response) -> Optional.empty();
    }

    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * Determine a unique browser/device fingerprint component for the provided request.
     *
     * @param principal The principal uid we are generating a fingerprint for.
     * @param request   the request
     * @param response  the response
     * @return The fingerprint component
     */
    Optional<String> extractComponent(String principal, HttpServletRequest request, HttpServletResponse response);
}
