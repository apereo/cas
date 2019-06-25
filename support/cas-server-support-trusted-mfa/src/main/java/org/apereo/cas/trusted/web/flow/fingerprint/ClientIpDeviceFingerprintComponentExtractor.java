package org.apereo.cas.trusted.web.flow.fingerprint;

import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * Provides the client ip for device fingerprint generation.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@Getter
@Setter
public class ClientIpDeviceFingerprintComponentExtractor implements DeviceFingerprintComponentExtractor {
    private int order = LOWEST_PRECEDENCE;

    @Override
    public Optional<String> extractComponent(final String principal,
                                             final RequestContext context,
                                             final boolean isNew) {
        return Optional.ofNullable(ClientInfoHolder.getClientInfo()).map(ClientInfo::getClientIpAddress);
    }
}
