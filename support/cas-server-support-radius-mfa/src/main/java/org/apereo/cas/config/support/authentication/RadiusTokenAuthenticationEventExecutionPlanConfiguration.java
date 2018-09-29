package org.apereo.cas.config.support.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.authentication.RadiusMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandler;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.RadiusMultifactorProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusServerProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
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
 * This is {@link RadiusTokenAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("radiusTokenAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RadiusTokenAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @RefreshScope
    @Bean
    public MultifactorAuthenticationProvider radiusAuthenticationProvider() {
        final RadiusMultifactorAuthenticationProvider p = new RadiusMultifactorAuthenticationProvider(radiusTokenServers());
        p.setBypassEvaluator(radiusBypassEvaluator());
        p.setFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getRadius().getRank());
        p.setId(casProperties.getAuthn().getMfa().getRadius().getId());
        return p;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusBypassEvaluator() {
        return MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getRadius().getBypass());
    }

    @RefreshScope
    @Bean
    public List<RadiusServer> radiusTokenServers() {
        final List<RadiusServer> list = new ArrayList<>();
        final RadiusClientProperties client = casProperties.getAuthn().getMfa().getRadius().getClient();
        final RadiusServerProperties server = casProperties.getAuthn().getMfa().getRadius().getServer();

        final RadiusClientFactory factory = new RadiusClientFactory(client.getAccountingPort(), client.getAuthenticationPort(), client.getSocketTimeout(),
                client.getInetAddress(), client.getSharedSecret());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(server.getProtocol());
        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory, server.getRetries(),
                server.getNasIpAddress(), server.getNasIpv6Address(),
                server.getNasPort(), server.getNasPortId(), server.getNasIdentifier(), server.getNasRealPort());

        list.add(impl);
        return list;
    }

    @ConditionalOnMissingBean(name = "radiusTokenPrincipalFactory")
    @Bean
    public PrincipalFactory radiusTokenPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public RadiusTokenAuthenticationHandler radiusTokenAuthenticationHandler() {
        final RadiusMultifactorProperties radius = casProperties.getAuthn().getMfa().getRadius();
        return new RadiusTokenAuthenticationHandler(radius.getName(), servicesManager, radiusTokenPrincipalFactory(), radiusTokenServers(),
                radius.isFailoverOnException(), radius.isFailoverOnAuthenticationFailure());
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator() {
        final String attribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
                attribute,
                radiusTokenAuthenticationHandler(),
                radiusAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "radiusTokenAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer radiusTokenAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(radiusTokenAuthenticationHandler());
            plan.registerMetadataPopulator(radiusAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(RadiusTokenCredential.class));
        };
    }
}
