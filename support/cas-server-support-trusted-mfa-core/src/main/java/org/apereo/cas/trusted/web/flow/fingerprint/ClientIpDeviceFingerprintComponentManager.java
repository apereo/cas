package org.apereo.cas.trusted.web.flow.fingerprint;

import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Provides the client ip for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class ClientIpDeviceFingerprintComponentManager implements DeviceFingerprintComponentManager {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal,
                                             final HttpServletRequest request,
                                             final HttpServletResponse response) {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo()).map(ClientInfo::getClientIpAddress);
    }
}
