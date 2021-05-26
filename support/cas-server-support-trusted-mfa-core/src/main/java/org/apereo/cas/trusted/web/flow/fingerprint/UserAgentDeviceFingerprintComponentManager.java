package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.util.HttpRequestUtils;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Provides the User Agent for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class UserAgentDeviceFingerprintComponentManager implements DeviceFingerprintComponentManager {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal,
                                             final HttpServletRequest request,
                                             final HttpServletResponse response) {
        return Optional.ofNullable(HttpRequestUtils.getHttpServletRequestUserAgent(request));
    }
}
