package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
@RefreshScope
public class RadiusMultifactorConfiguration {

    /**
     * The Protocol.
     */
    @Value("${cas.mfa.radius.server.protocol:EAP_MSCHAPv2}")
    private RadiusProtocol protocol;

    /**
     * The Retries.
     */
    @Value("${cas.mfa.radius.server.retries:3}")
    private int retries;

    /**
     * The Nas identifier.
     */
    @Value("${cas.mfa.radius.server.nasIdentifier:-1}")
    private long nasIdentifier;

    /**
     * The Nas port.
     */
    @Value("${cas.mfa.radius.server.nasPort:-1}")
    private long nasPort;

    /**
     * The Nas port id.
     */
    @Value("${cas.mfa.radius.server.nasPortId:-1}")
    private long nasPortId;

    /**
     * The Nas real port.
     */
    @Value("${cas.mfa.radius.server.nasRealPort:-1}")
    private long nasRealPort;

    /**
     * The Nas port type.
     */
    @Value("${cas.mfa.radius.server.nasPortType:-1}")
    private int nasPortType;

    /**
     * The Nas ip.
     */
    @Value("${cas.mfa.radius.server.nasIpAddress:}")
    private String nasIp;

    /**
     * The Nas ipv 6.
     */
    @Value("${cas.mfa.radius.server.nasIpv6Address:}")
    private String nasIpv6;

    /**
     * The Inet address.
     */
    @Value("${cas.mfa.radius.client.inetaddr:localhost}")
    private String inetAddress;

    /**
     * The Accounting port.
     */
    @Value("${cas.mfa.radius.client.port.acct:" + RadiusServer.DEFAULT_PORT_ACCOUNTING + "}")
    private int accountingPort;

    /**
     * The Authentication port.
     */
    @Value("${cas.mfa.radius.client.port.authn:" + RadiusServer.DEFAULT_PORT_AUTHENTICATION + "}")
    private int authenticationPort;

    /**
     * The Socket timeout.
     */
    @Value("${cas.mfa.radius.client.socket.timeout:5}")
    private int socketTimeout;

    /**
     * The Shared secret.
     */
    @Value("${cas.mfa.radius.client.sharedsecret:N0Sh@ar3d$ecReT}")
    private String sharedSecret;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("builder")
    private FlowBuilderServices builder;

    /**
     * Radius flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean(name = "radiusFlowRegistry")
    public FlowDefinitionRegistry radiusFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-radius/*-webflow.xml");
        return builder.build();
    }
    

    /**
     * Radius servers list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean(name="radiusTokenServers")
    public List radiusTokenServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();

        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(this.accountingPort);
        factory.setAuthenticationPort(this.authenticationPort);
        factory.setInetAddress(this.inetAddress);
        factory.setSharedSecret(this.sharedSecret);
        factory.setSocketTimeout(this.socketTimeout);

        final JRadiusServerImpl impl = new JRadiusServerImpl(this.protocol, factory);
        impl.setRetries(this.retries);
        impl.setNasIdentifier(this.nasIdentifier);
        impl.setNasPort(this.nasPort);
        impl.setNasPortId(this.nasPortId);
        impl.setNasRealPort(this.nasRealPort);
        impl.setNasIpAddress(this.nasIp);
        impl.setNasIpv6Address(this.nasIpv6);
        
        list.add(impl);
        return list;
    }
}
