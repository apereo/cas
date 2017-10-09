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

    /**
     * The port to do accounting on.
     */
    private int accountingPort = RadiusServer.DEFAULT_PORT_ACCOUNTING;

    /**
     * The port to do authentication on.
     */
    private int authenticationPort = RadiusServer.DEFAULT_PORT_AUTHENTICATION;

    /**
     * Socket timeout in seconds.
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * RADIUS server network address.
     */
    private InetAddress inetAddress;

    /**
     * The shared secret to send to the RADIUS server.
     */
    private final String sharedSecret;

    public RadiusClientFactory(final int accountingPort, final int authenticationPort, final String inetAddress, final String sharedSecret) {
        this(accountingPort, authenticationPort, DEFAULT_SOCKET_TIMEOUT, inetAddress, sharedSecret);
    }

    /**
     * @param accountingPort     Sets the RADIUS server accounting port.
     * @param authenticationPort Sets the RADIUS server authentication port.
     * @param socketTimeout      Sets the RADIUS server UDP socket timeout.
     * @param inetAddress        RADIUS server network address.
     * @param sharedSecret       RADIUS server authentication shared secret.
     */
    public RadiusClientFactory(final int accountingPort, final int authenticationPort,
                               final int socketTimeout,
                               final String inetAddress,
                               final String sharedSecret) {
        this.accountingPort = accountingPort;
        this.authenticationPort = authenticationPort;
        this.socketTimeout = socketTimeout;
        try {
            this.inetAddress = InetAddress.getByName(inetAddress);
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        this.sharedSecret = sharedSecret;
    }

    /**
     * Creates a new RADIUS client instance using factory configuration settings.
     *
     * @return New radius client instance.
     * @throws IOException In case the transport method encounters an error.
     */
    public RadiusClient newInstance() throws IOException {
        return new RadiusClient(this.inetAddress, this.sharedSecret, this.authenticationPort, this.accountingPort, this.socketTimeout);
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

