package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusServerProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("radiusConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RadiusConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("radiusPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "radiusPrincipalFactory")
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
        final RadiusClientProperties client = casProperties.getAuthn().getRadius().getClient();
        final RadiusServerProperties server = casProperties.getAuthn().getRadius().getServer();

        final RadiusClientFactory factory = new RadiusClientFactory(client.getAccountingPort(), client.getAuthenticationPort(), client.getSocketTimeout(),
                client.getInetAddress(), client.getSharedSecret());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(server.getProtocol());

        return new JRadiusServerImpl(protocol, factory, server.getRetries(),
                server.getNasIpAddress(), server.getNasIpv6Address(), server.getNasPort(),
                server.getNasPortId(), server.getNasIdentifier(), server.getNasRealPort());
    }

    /**
     * Radius servers list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean
    public List<RadiusServer> radiusServers() {
        final List<RadiusServer> list = new ArrayList<>();
        list.add(radiusServer());
        return list;
    }

    @Bean
    public AuthenticationHandler radiusAuthenticationHandler() {
        final RadiusProperties radius = casProperties.getAuthn().getRadius();
        final RadiusAuthenticationHandler h = new RadiusAuthenticationHandler(radius.getName(), servicesManager, radiusPrincipalFactory(), radiusServers(),
                radius.isFailoverOnException(), radius.isFailoverOnAuthenticationFailure());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(radius.getPasswordEncoder()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(radius.getPrincipalTransformation()));

        if (passwordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(passwordPolicyConfiguration);
        }
        return h;
    }

    @ConditionalOnMissingBean(name = "radiusAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer radiusAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getRadius().getClient().getInetAddress())) {
                plan.registerAuthenticationHandler(radiusAuthenticationHandler());
            } else {
                LOGGER.warn("No RADIUS address is defined. RADIUS support will be disabled.");
            }
        };
    }
}
