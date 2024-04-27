package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.Setter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Provides the User Agent for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class UserAgentDeviceFingerprintExtractor implements DeviceFingerprintExtractor {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extract(final Authentication authentication,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {
        return Optional.ofNullable(HttpRequestUtils.getHttpServletRequestUserAgent(request));
    }
}
