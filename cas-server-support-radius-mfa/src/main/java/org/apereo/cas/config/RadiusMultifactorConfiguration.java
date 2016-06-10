package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.RadiusMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandler;
import org.apereo.cas.adaptors.radius.web.RadiusMultifactorApplicationContextWrapper;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
public class RadiusMultifactorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Resource(name = "builder")
    private FlowBuilderServices builder;

    /**
     * Radius flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean
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
    @Bean
    public List radiusTokenServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();

        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(casProperties.getMfa().getRadius().getClient().getAccountingPort());
        factory.setAuthenticationPort(casProperties.getMfa().getRadius().getClient().getAuthenticationPort());
        factory.setInetAddress(casProperties.getMfa().getRadius().getClient().getInetAddress());
        factory.setSharedSecret(casProperties.getMfa().getRadius().getClient().getSharedSecret());
        factory.setSocketTimeout(casProperties.getMfa().getRadius().getClient().getSocketTimeout());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(casProperties.getMfa().getRadius().getServer().getProtocol());

        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory);
        impl.setRetries(casProperties.getMfa().getRadius().getServer().getRetries());
        impl.setNasIdentifier(casProperties.getMfa().getRadius().getServer().getNasIdentifier());
        impl.setNasPort(casProperties.getMfa().getRadius().getServer().getNasPort());
        impl.setNasPortId(casProperties.getMfa().getRadius().getServer().getNasPortId());
        impl.setNasRealPort(casProperties.getMfa().getRadius().getServer().getNasRealPort());
        impl.setNasIpAddress(casProperties.getMfa().getRadius().getServer().getNasIpAddress());
        impl.setNasIpv6Address(casProperties.getMfa().getRadius().getServer().getNasIpv6Address());

        list.add(impl);
        return list;
    }

    @RefreshScope
    @Bean
    public MultifactorAuthenticationProvider radiusAuthenticationProvider() {
        return new RadiusMultifactorAuthenticationProvider();
    }

    @RefreshScope
    @Bean
    public RadiusTokenAuthenticationHandler radiusTokenAuthenticationHandler() {
        final RadiusTokenAuthenticationHandler a = new RadiusTokenAuthenticationHandler();

        a.setServers(radiusTokenServers());
        a.setFailoverOnAuthenticationFailure(casProperties.getMfa().getRadius().isFailoverOnAuthenticationFailure());
        a.setFailoverOnException(casProperties.getMfa().getRadius().isFailoverOnException());
        
        return a;
    }

    @Bean
    public BaseApplicationContextWrapper radiusMultifactorApplicationContextWrapper() {
        return new RadiusMultifactorApplicationContextWrapper();
    }

    @Bean
    public Action radiusAuthenticationWebflowAction() {
        return new RadiusAuthenticationWebflowAction();
    }

    @Bean
    public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver() {
        return new RadiusAuthenticationWebflowEventResolver();
    }

    @Bean
    public CasWebflowConfigurer radiusMultifactorWebflowConfigurer() {
        return new RadiusMultifactorWebflowConfigurer();
    }
}
