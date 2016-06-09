package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.adaptors.radius.web.RadiusApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RadiusProperties properties;

    /**
     * Radius server j radius server.
     *
     * @return the j radius server
     */
    @RefreshScope
    @Bean
    public JRadiusServerImpl radiusServer() {
        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(properties.getClient().getAccountingPort());
        factory.setAuthenticationPort(properties.getClient().getAuthenticationPort());
        factory.setInetAddress(properties.getClient().getInetAddress());
        factory.setSharedSecret(properties.getClient().getSharedSecret());
        factory.setSocketTimeout(properties.getClient().getSocketTimeout());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(properties.getServer().getProtocol());

        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory);
        impl.setRetries(properties.getServer().getRetries());
        impl.setNasIdentifier(properties.getServer().getNasIdentifier());
        impl.setNasPort(properties.getServer().getNasPort());
        impl.setNasPortId(properties.getServer().getNasPortId());
        impl.setNasRealPort(properties.getServer().getNasRealPort());
        impl.setNasIpAddress(properties.getServer().getNasIpAddress());
        impl.setNasIpv6Address(properties.getServer().getNasIpv6Address());

        return impl;
    }

    /**
     * Radius servers list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean
    public List radiusServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();
        list.add(radiusServer());
        return list;
    }

    @Bean
    public AuthenticationHandler radiusAuthenticationHandler() {
        final RadiusAuthenticationHandler h = new RadiusAuthenticationHandler();
        
        h.setFailoverOnAuthenticationFailure(properties.isFailoverOnAuthenticationFailure());
        h.setFailoverOnException(properties.isFailoverOnException());
        h.setServers(radiusServers());
        return h;
    }

    @Bean
    public BaseApplicationContextWrapper radiusApplicationContextWrapper() {
        return new RadiusApplicationContextWrapper();
    }
}
