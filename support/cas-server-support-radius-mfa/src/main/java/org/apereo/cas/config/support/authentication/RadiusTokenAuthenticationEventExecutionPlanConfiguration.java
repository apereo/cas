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
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link RadiusTokenAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.authn.mfa.radius.client.inet-address")
@Configuration(value = "radiusTokenAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class RadiusTokenAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "RadiusTokenAuthenticationServerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusTokenAuthenticationServerConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusTokenServers")
        @Autowired
        public BeanContainer<RadiusServer> radiusTokenServers(
            final CasConfigurationProperties casProperties,
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext) {
            val radius = casProperties.getAuthn().getMfa().getRadius();
            val client = radius.getClient();
            val server = radius.getServer();
            if (StringUtils.isBlank(client.getInetAddress())) {
                return BeanContainer.empty();
            }
            val factory = RadiusClientFactory.builder()
                .authenticationPort(client.getAccountingPort())
                .authenticationPort(client.getAuthenticationPort())
                .socketTimeout(client.getSocketTimeout())
                .inetAddress(client.getInetAddress())
                .sharedSecret(client.getSharedSecret())
                .sslContext(casSslContext)
                .transportType(client.getTransportType())
                .build();
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
            return BeanContainer.of(CollectionUtils.wrapList(impl));
        }

        @ConditionalOnMissingBean(name = "radiusTokenPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory radiusTokenPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusTokenAuthenticationEventExecutionPlanProviderConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusMultifactorAuthenticationProvider")
        @Autowired
        public MultifactorAuthenticationProvider radiusMultifactorAuthenticationProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenServers")
            final BeanContainer<RadiusServer> radiusTokenServers,
            @Qualifier("radiusBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            val radius = casProperties.getAuthn().getMfa().getRadius();
            val p = new RadiusMultifactorAuthenticationProvider(radiusTokenServers.toList());
            p.setBypassEvaluator(radiusBypassEvaluator);
            p.setFailureMode(radius.getFailureMode());
            p.setFailureModeEvaluator(failureModeEvaluator);
            p.setOrder(radius.getRank());
            p.setId(radius.getId());
            return p;
        }
    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusTokenAuthenticationEventExecutionPlanHandlerConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusTokenAuthenticationHandler")
        @Autowired
        public AuthenticationHandler radiusTokenAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenPrincipalFactory")
            final PrincipalFactory radiusTokenPrincipalFactory,
            @Qualifier("radiusTokenServers")
            final BeanContainer<RadiusServer> radiusTokenServers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val radius = casProperties.getAuthn().getMfa().getRadius();
            return new RadiusTokenAuthenticationHandler(radius.getName(), servicesManager,
                radiusTokenPrincipalFactory, radiusTokenServers.toList(), radius.isFailoverOnException(),
                radius.isFailoverOnAuthenticationFailure(), radius.getOrder());
        }
    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusTokenAuthenticationEventExecutionPlanMetadataConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "radiusAuthenticationMetaDataPopulator")
        @Autowired
        public AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenAuthenticationHandler")
            final AuthenticationHandler radiusTokenAuthenticationHandler,
            @Qualifier("radiusMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider radiusMultifactorAuthenticationProvider) {
            val attribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new AuthenticationContextAttributeMetaDataPopulator(attribute, radiusTokenAuthenticationHandler, radiusMultifactorAuthenticationProvider.getId());
        }

    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class RadiusTokenAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "radiusTokenAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationEventExecutionPlanConfigurer radiusTokenAuthenticationEventExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenAuthenticationHandler")
            final AuthenticationHandler radiusTokenAuthenticationHandler,
            @Qualifier("radiusAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator) {
            return plan -> {
                val radius = casProperties.getAuthn().getMfa().getRadius();
                val client = radius.getClient();
                if (StringUtils.isNotBlank(client.getInetAddress())) {
                    plan.registerAuthenticationHandler(radiusTokenAuthenticationHandler);
                    plan.registerAuthenticationMetadataPopulator(radiusAuthenticationMetaDataPopulator);
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(RadiusTokenCredential.class));
                }
            };
        }
    }
}
