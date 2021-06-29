package org.apereo.cas.trusted.web.flow.fingerprint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Interface for determining a device fingerprint for usage within MFA trusted device records.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
public interface DeviceFingerprintStrategy {
    /**
     * Default implementation bean name.
     */
    String DEFAULT_BEAN_NAME = "deviceFingerprintStrategy";

    /**
     * Determine a unique browser/device fingerprint for the provided request.
     *
     * @param principal The principal uid we are generating a fingerprint for.
     * @param request   the request
     * @param response  the response
     * @return The generated fingerprint
     */
    String determineFingerprintComponent(String principal,
                                         HttpServletRequest request,
                                         HttpServletResponse response);

    /**
     * Gets device fingerprint component extractors.
     *
     * @return the device fingerprint component extractors
     */
    List<DeviceFingerprintComponentManager> getDeviceFingerprintComponentManagers();
}
