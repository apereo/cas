package org.apereo.cas.adaptors.radius;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.jradius.client.RadiusClient;

import java.net.InetAddress;

/**
 * Factory for creating RADIUS client instances.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@ToString
@AllArgsConstructor
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
    private String inetAddress;

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

    /**
     * Creates a new RADIUS client instance using factory configuration settings.
     *
     * @return New radius client instance.
     */
    @SneakyThrows
    public RadiusClient newInstance() {
        return new RadiusClient(InetAddress.getByName(this.inetAddress), this.sharedSecret, this.authenticationPort, this.accountingPort, this.socketTimeout);
    }
}
