package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RadiusServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-radius")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RadiusServerProperties")
public class RadiusServerProperties implements Serializable {

    private static final long serialVersionUID = -3911282132573730184L;

    /**
     * Radius protocol to use when communicating with the server.
     */
    private String protocol = "EAP_MSCHAPv2";

    /**
     * Number of re-try attempts when dealing with connection and authentication failures.
     */
    private int retries = 3;

    /**
     * The NAS identifier.
     */
    private String nasIdentifier;

    /**
     * The NAS port.
     */
    private long nasPort = -1;

    /**
     * The NAS port id.
     */
    private long nasPortId = -1;

    /**
     * The NAS real port.
     */
    private long nasRealPort = -1;

    /**
     * The NAS port type.
     */
    private int nasPortType = -1;

    /**
     * The NAS IP address.
     */
    private String nasIpAddress;

    /**
     * The NAS IPv6 address.
     */
    private String nasIpv6Address;
}
