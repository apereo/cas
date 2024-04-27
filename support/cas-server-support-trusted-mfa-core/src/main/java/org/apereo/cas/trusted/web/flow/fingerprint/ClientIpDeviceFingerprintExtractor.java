package org.apereo.cas.trusted.web.flow.fingerprint;

import org.apereo.cas.authentication.Authentication;
import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Provides the client ip for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class ClientIpDeviceFingerprintExtractor implements DeviceFingerprintExtractor {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extract(final Authentication authentication,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo()).map(ClientInfo::getClientIpAddress);
    }
}
