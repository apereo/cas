package org.apereo.cas.adaptors.radius;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import net.jradius.client.RadiusClient;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Factory for creating RADIUS client instances.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@AllArgsConstructor
public class RadiusClientFactory implements Serializable {

    private static final int DEFAULT_SOCKET_TIMEOUT = 60;
    private static final long serialVersionUID = 8226097527127614276L;
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
    private String inetAddress;

    /**
     * The shared secret to send to the RADIUS server.
     */
    private final String sharedSecret;

    public RadiusClientFactory(final int accountingPort, final int authenticationPort,
                               final String inetAddress, final String sharedSecret) {
        this(accountingPort, authenticationPort, DEFAULT_SOCKET_TIMEOUT, inetAddress, sharedSecret);
    }

    public RadiusClientFactory(final String inetAddress, final String sharedSecret) {
        this(RadiusServer.DEFAULT_PORT_ACCOUNTING, RadiusServer.DEFAULT_PORT_AUTHENTICATION,
            DEFAULT_SOCKET_TIMEOUT, inetAddress, sharedSecret);
    }


    /**
     * New instance radius client.
     *
     * @return the radius client
     */
    @SneakyThrows
    public RadiusClient newInstance() {
        return new RadiusClient(InetAddress.getByName(this.inetAddress), this.sharedSecret,
            this.authenticationPort, this.accountingPort, this.socketTimeout);
    }
}
