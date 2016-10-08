package org.apereo.cas.adaptors.radius;

import net.jradius.client.RadiusClient;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Factory for creating RADIUS client instances.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class RadiusClientFactory {

    private static final int DEFAULT_SOCKET_TIMEOUT = 60;

    /** The port to do accounting on. */
    private int accountingPort = RadiusServer.DEFAULT_PORT_ACCOUNTING;

    /** The port to do authentication on. */
    private int authenticationPort = RadiusServer.DEFAULT_PORT_AUTHENTICATION;

    /** Socket timeout in seconds. */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /** RADIUS server network address. */
    private InetAddress inetAddress;

    /** The shared secret to send to the RADIUS server. */
    private String sharedSecret;

    /**
     * Sets the RADIUS server accounting port.
     *
     * @param port Accounting port number.
     */
    public void setAccountingPort(final int port) {
        this.accountingPort = port;
    }

    /**
     * Sets the RADIUS server authentication port.
     *
     * @param port Authentication port number.
     */
    public void setAuthenticationPort(final int port) {
        this.authenticationPort = port;
    }

    /**
     * Sets the RADIUS server UDP socket timeout.
     *
     * @param timeout Timeout in seconds; 0 for no timeout.
     */
    public void setSocketTimeout(final int timeout) {
        this.socketTimeout = timeout;
    }

    /**
     * RADIUS server network address.
     *
     * @param address Network address as a string.
     */
    public void setInetAddress(final String address) {
        try {
            this.inetAddress = InetAddress.getByName(address);
        } catch (final UnknownHostException e) {
            throw new RuntimeException("Invalid address " + address);
        }
    }

    /**
     * RADIUS server authentication shared secret.
     *
     * @param secret Shared secret.
     */
    public void setSharedSecret(final String secret) {
        this.sharedSecret = secret;
    }

    /**
     * Creates a new RADIUS client instance using factory configuration settings.
     *
     * @return New radius client instance.
     * @throws IOException In case the transport method encounters an error.
     */
    public RadiusClient newInstance() throws IOException {
        return new RadiusClient(
                this.inetAddress, this.sharedSecret, this.authenticationPort, this.accountingPort, this.socketTimeout);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("accountingPort", this.accountingPort)
                .append("authenticationPort", this.authenticationPort)
                .append("socketTimeout", this.socketTimeout)
                .append("inetAddress", this.inetAddress)
                .toString();
    }
}

