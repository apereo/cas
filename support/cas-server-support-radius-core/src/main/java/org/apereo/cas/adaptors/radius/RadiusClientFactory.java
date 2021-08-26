package org.apereo.cas.adaptors.radius;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.val;
import net.jradius.client.RadiusClient;
import net.jradius.radsec.RadSecClientTransport;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Factory for creating RADIUS client instances.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString(exclude = {"sharedSecret", "sslContext"})
@SuperBuilder
public class RadiusClientFactory implements Serializable {

    private static final int DEFAULT_SOCKET_TIMEOUT = 60;

    private static final long serialVersionUID = 8226097527127614276L;

    @Builder.Default
    private final int accountingPort = RadiusServer.DEFAULT_PORT_ACCOUNTING;

    @Builder.Default
    private final int authenticationPort = RadiusServer.DEFAULT_PORT_AUTHENTICATION;

    @Builder.Default
    private final int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    private final String inetAddress;

    private final String sharedSecret;

    private final CasSSLContext sslContext;

    @Builder.Default
    private final RadiusClientProperties.RadiusClientTransportTypes transportType
        = RadiusClientProperties.RadiusClientTransportTypes.UDP;

    /**
     * New instance radius client.
     * Attempts to pre-load authenticators
     * that are defined statically before
     * returning the client.
     *
     * @return the radius client
     */
    @SneakyThrows
    public RadiusClient newInstance() {
        if (sslContext != null && this.transportType == RadiusClientProperties.RadiusClientTransportTypes.RADSEC) {
            val transport = new RadSecClientTransport(sslContext.getKeyManagers(), sslContext.getTrustManagers());
            transport.setRemoteInetAddress(InetAddress.getByName(this.inetAddress));
            transport.setSharedSecret(sharedSecret);
            transport.setAuthPort(this.authenticationPort);
            transport.setAcctPort(this.accountingPort);
            transport.setSocketTimeout(this.socketTimeout);
            return new RadiusClient(transport);
        }
        return new RadiusClient(InetAddress.getByName(this.inetAddress), this.sharedSecret,
            this.authenticationPort, this.accountingPort, this.socketTimeout);
    }
}
