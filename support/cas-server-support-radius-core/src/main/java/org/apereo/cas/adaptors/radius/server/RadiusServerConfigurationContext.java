package org.apereo.cas.adaptors.radius.server;

import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * This is {@link RadiusServerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class RadiusServerConfigurationContext implements Serializable {
    private static final long serialVersionUID = -7593796856411325124L;

    private final RadiusProtocol protocol;
    private final RadiusClientFactory radiusClientFactory;

    private final String nasIpAddress;
    private final String nasIpv6Address;
    private final String nasIdentifier;

    /**
     * Number of times to retry authentication
     * when no response is received.
     */
    private final int retries;

    @Builder.Default
    private final long nasPort = -1;
    @Builder.Default
    private final long nasPortId = -1;
    @Builder.Default
    private final long nasRealPort = -1;

    @Builder.Default
    private final long nasPortType = -1;
}
