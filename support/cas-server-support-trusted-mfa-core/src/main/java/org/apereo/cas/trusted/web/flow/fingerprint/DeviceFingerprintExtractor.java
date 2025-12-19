package org.apereo.cas.trusted.web.flow.fingerprint;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface for extracting a device fingerprint component for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@FunctionalInterface
public interface DeviceFingerprintExtractor extends Ordered {
    /**
     * Return a no-op DeviceFingerprintComponent.
     *
     * @return a no-op DeviceFingerprintComponent.
     */
    static DeviceFingerprintExtractor noOp() {
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
     * @throws Throwable the throwable
     */
    Optional<String> extract(Authentication principal, HttpServletRequest request, HttpServletResponse response) throws Throwable;
}
