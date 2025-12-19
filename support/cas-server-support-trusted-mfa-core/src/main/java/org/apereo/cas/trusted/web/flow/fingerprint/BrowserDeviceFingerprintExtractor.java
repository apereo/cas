package org.apereo.cas.trusted.web.flow.fingerprint;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Extracts the browser fingerprint from the request
 * as collected during authentication attempts from the client.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
public class BrowserDeviceFingerprintExtractor implements DeviceFingerprintExtractor {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extract(final Authentication authentication,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo().getDeviceFingerprint());
    }
}
