package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusConfiguration {

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("radiusPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PrincipalFactory radiusPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    /**
     * Radius server j radius server.
     *
     * @return the j radius server
     */
    @RefreshScope
    @Bean
    public JRadiusServerImpl radiusServer() {

        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(casProperties.getAuthn().getRadius().getClient().getAccountingPort());
        factory.setAuthenticationPort(casProperties.getAuthn().getRadius().getClient().getAuthenticationPort());
        factory.setInetAddress(casProperties.getAuthn().getRadius().getClient().getInetAddress());
        factory.setSharedSecret(casProperties.getAuthn().getRadius().getClient().getSharedSecret());
        factory.setSocketTimeout(casProperties.getAuthn().getRadius().getClient().getSocketTimeout());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(
                casProperties.getAuthn().getRadius().getServer().getProtocol());

        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory);
        impl.setRetries(casProperties.getAuthn().getRadius().getServer().getRetries());
        impl.setNasIdentifier(casProperties.getAuthn().getRadius().getServer().getNasIdentifier());
        impl.setNasPort(casProperties.getAuthn().getRadius().getServer().getNasPort());
        impl.setNasPortId(casProperties.getAuthn().getRadius().getServer().getNasPortId());
        impl.setNasRealPort(casProperties.getAuthn().getRadius().getServer().getNasRealPort());
        impl.setNasIpAddress(casProperties.getAuthn().getRadius().getServer().getNasIpAddress());
        impl.setNasIpv6Address(casProperties.getAuthn().getRadius().getServer().getNasIpv6Address());

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

        h.setFailoverOnAuthenticationFailure(casProperties.getAuthn().getRadius().isFailoverOnAuthenticationFailure());
        h.setFailoverOnException(casProperties.getAuthn().getRadius().isFailoverOnException());
        h.setServers(radiusServers());

        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getRadius().getPasswordEncoder()));
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getRadius().getPrincipalTransformation()));

        if (passwordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(passwordPolicyConfiguration);
        }

        h.setPrincipalFactory(radiusPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getRadius().getClient().getInetAddress())) {
            authenticationHandlersResolvers.put(radiusAuthenticationHandler(), null);
        }
    }
}


