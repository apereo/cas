package org.apereo.cas.configuration.model.support.radius;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link RadiusServerProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-radius")
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(final int retries) {
        this.retries = retries;
    }

    public String getNasIdentifier() {
        return nasIdentifier;
    }

    public void setNasIdentifier(final String nasIdentifier) {
        this.nasIdentifier = nasIdentifier;
    }

    public long getNasPort() {
        return nasPort;
    }

    public void setNasPort(final long nasPort) {
        this.nasPort = nasPort;
    }

    public long getNasPortId() {
        return nasPortId;
    }

    public void setNasPortId(final long nasPortId) {
        this.nasPortId = nasPortId;
    }

    public long getNasRealPort() {
        return nasRealPort;
    }

    public void setNasRealPort(final long nasRealPort) {
        this.nasRealPort = nasRealPort;
    }

    public int getNasPortType() {
        return nasPortType;
    }

    public void setNasPortType(final int nasPortType) {
        this.nasPortType = nasPortType;
    }

    public String getNasIpAddress() {
        return nasIpAddress;
    }

    public void setNasIpAddress(final String nasIpAddress) {
        this.nasIpAddress = nasIpAddress;
    }

    public String getNasIpv6Address() {
        return nasIpv6Address;
    }

    public void setNasIpv6Address(final String nasIpv6Address) {
        this.nasIpv6Address = nasIpv6Address;
    }
}
