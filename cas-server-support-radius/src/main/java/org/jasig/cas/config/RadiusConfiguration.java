package org.jasig.cas.config;

import org.jasig.cas.adaptors.radius.JRadiusServerImpl;
import org.jasig.cas.adaptors.radius.RadiusClientFactory;
import org.jasig.cas.adaptors.radius.RadiusProtocol;
import org.jasig.cas.adaptors.radius.RadiusServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("radiusConfiguration")
public class RadiusConfiguration {
    
    @Value("${cas.radius.server.protocol:EAP_MSCHAPv2}")
    private RadiusProtocol protocol;

    @Value("${cas.radius.server.retries:3}")
    private int retries;

    @Value("${cas.radius.server.nasIdentifier:-1}")
    private long nasIdentifier;
    
    @Value("${cas.radius.server.nasPort:-1}")
    private long nasPort;
    
    @Value("${cas.radius.server.nasPortId:-1}")
    private long nasPortId;
    
    @Value("${cas.radius.server.nasRealPort:-1}")
    private long nasRealPort;
    
    @Value("${cas.radius.server.nasPortType:-1}")
    private int nasPortType;
    
    @Value("${cas.radius.server.nasIpAddress:}")
    private String nasIp;
    
    @Value("${cas.radius.server.nasIpv6Address:}")
    private String nasIpv6;
    
    @Value("${cas.radius.client.inetaddr:localhost}")
    private String inetAddress;
    
    @Value("${cas.radius.client.port.acct:" + RadiusServer.DEFAULT_PORT_ACCOUNTING + "}")
    private int accountingPort;

    @Value("${cas.radius.client.port.authn:" + RadiusServer.DEFAULT_PORT_AUTHENTICATION + "}")
    private int authenticationPort;
    
    @Value("${cas.radius.client.socket.timeout:60}")
    private int socketTimeout;
    
    @Value("${cas.radius.client.sharedsecret:N0Sh@ar3d$ecReT}")
    private String sharedSecret; 
    
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
    
    @Bean(name="radiusServers")
    public List radiusServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();
        list.add(radiusServer());
        return list;
    }
    
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
