package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusConfiguration")
public class RadiusConfiguration {

    /**
     * The Protocol.
     */
    @Value("${cas.radius.server.protocol:EAP_MSCHAPv2}")
    private RadiusProtocol protocol;

    /**
     * The Retries.
     */
    @Value("${cas.radius.server.retries:3}")
    private int retries;

    /**
     * The Nas identifier.
     */
    @Value("${cas.radius.server.nasIdentifier:-1}")
    private long nasIdentifier;

    /**
     * The Nas port.
     */
    @Value("${cas.radius.server.nasPort:-1}")
    private long nasPort;

    /**
     * The Nas port id.
     */
    @Value("${cas.radius.server.nasPortId:-1}")
    private long nasPortId;

    /**
     * The Nas real port.
     */
    @Value("${cas.radius.server.nasRealPort:-1}")
    private long nasRealPort;

    /**
     * The Nas port type.
     */
    @Value("${cas.radius.server.nasPortType:-1}")
    private int nasPortType;

    /**
     * The Nas ip.
     */
    @Value("${cas.radius.server.nasIpAddress:}")
    private String nasIp;

    /**
     * The Nas ipv 6.
     */
    @Value("${cas.radius.server.nasIpv6Address:}")
    private String nasIpv6;

    /**
     * The Inet address.
     */
    @Value("${cas.radius.client.inetaddr:localhost}")
    private String inetAddress;

    /**
     * The Accounting port.
     */
    @Value("${cas.radius.client.port.acct:" + RadiusServer.DEFAULT_PORT_ACCOUNTING + "}")
    private int accountingPort;

    /**
     * The Authentication port.
     */
    @Value("${cas.radius.client.port.authn:" + RadiusServer.DEFAULT_PORT_AUTHENTICATION + "}")
    private int authenticationPort;

    /**
     * The Socket timeout.
     */
    @Value("${cas.radius.client.socket.timeout:5}")
    private int socketTimeout;

    /**
     * The Shared secret.
     */
    @Value("${cas.radius.client.sharedsecret:N0Sh@ar3d$ecReT}")
    private String sharedSecret;

    /**
     * Radius server j radius server.
     *
     * @return the j radius server
     */
    @RefreshScope
    @Bean(name="radiusServer")
    public JRadiusServerImpl radiusServer() {
        final JRadiusServerImpl impl = new JRadiusServerImpl(this.protocol, radiusClientFactory());
        impl.setRetries(this.retries);
        impl.setNasIdentifier(this.nasIdentifier);
        impl.setNasPort(this.nasPort);
        impl.setNasPortId(this.nasPortId);
        impl.setNasRealPort(this.nasRealPort);
        impl.setNasIpAddress(this.nasIp);
        impl.setNasIpv6Address(this.nasIpv6);
        return impl;
    }

    /**
     * Radius servers list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean(name="radiusServers")
    public List radiusServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();
        list.add(radiusServer());
        return list;
    }

    /**
     * Radius client factory radius client factory.
     *
     * @return the radius client factory
     */
    @RefreshScope
    @Bean(name="radiusClientFactory")
    public RadiusClientFactory radiusClientFactory() {
        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(this.accountingPort);
        factory.setAuthenticationPort(this.authenticationPort);
        factory.setInetAddress(this.inetAddress);
        factory.setSharedSecret(this.sharedSecret);
        factory.setSocketTimeout(this.socketTimeout);
        return factory;
    }
    
}
