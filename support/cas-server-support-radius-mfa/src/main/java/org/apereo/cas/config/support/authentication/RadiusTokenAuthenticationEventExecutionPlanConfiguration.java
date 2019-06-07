package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.authentication.RadiusMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandler;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.adaptors.radius.server.NonBlockingRadiusServer;
import org.apereo.cas.adaptors.radius.server.RadiusServerConfigurationContext;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
public class RadiusTokenAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("radiusBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> radiusBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @RefreshScope
    @Bean
    public MultifactorAuthenticationProvider radiusMultifactorAuthenticationProvider() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val p = new RadiusMultifactorAuthenticationProvider(radiusTokenServers());
        p.setBypassEvaluator(radiusBypassEvaluator.getIfAvailable());
        p.setFailureMode(radius.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getIfAvailable());
        p.setOrder(radius.getRank());
        p.setId(radius.getId());
        return p;
    }

    @RefreshScope
    @Bean
    public List<RadiusServer> radiusTokenServers() {
        val list = new ArrayList<RadiusServer>();
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val client = radius.getClient();
        val server = radius.getServer();

        val factory = new RadiusClientFactory(client.getAccountingPort(), client.getAuthenticationPort(), client.getSocketTimeout(),
            client.getInetAddress(), client.getSharedSecret());

        val protocol = RadiusProtocol.valueOf(server.getProtocol());
        val context = RadiusServerConfigurationContext.builder()
            .protocol(protocol)
            .radiusClientFactory(factory)
            .retries(server.getRetries())
            .nasIpAddress(server.getNasIpAddress())
            .nasIpv6Address(server.getNasIpv6Address())
            .nasPort(server.getNasPort())
            .nasPortId(server.getNasPortId())
            .nasIdentifier(server.getNasIdentifier())
            .nasRealPort(server.getNasRealPort())
            .nasPortType(server.getNasPortType())
            .build();
        val impl = new NonBlockingRadiusServer(context);
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
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RadiusTokenAuthenticationHandler(radius.getName(), servicesManager.getIfAvailable(),
            radiusTokenPrincipalFactory(), radiusTokenServers(),
            radius.isFailoverOnException(),
            radius.isFailoverOnAuthenticationFailure(),
            radius.getOrder());
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator() {
        val attribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(attribute,
            radiusTokenAuthenticationHandler(),
            radiusMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "radiusTokenAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer radiusTokenAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(radiusTokenAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(radiusAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(RadiusTokenCredential.class));
        };
    }
}
