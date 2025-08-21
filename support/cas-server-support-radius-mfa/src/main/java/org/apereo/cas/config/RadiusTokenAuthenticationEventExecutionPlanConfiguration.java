package org.apereo.cas.config;

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
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.RadiusMFA)
@Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class RadiusTokenAuthenticationEventExecutionPlanConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.radius.client.inet-address");

    @Configuration(value = "RadiusTokenAuthenticationServerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusTokenAuthenticationServerConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusTokenServers")
        public BeanContainer<RadiusServer> radiusTokenServers(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
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
    static class RadiusTokenAuthenticationEventExecutionPlanProviderConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusMultifactorAuthenticationProvider")
        public MultifactorAuthenticationProvider radiusMultifactorAuthenticationProvider(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenServers")
            final BeanContainer<RadiusServer> radiusTokenServers,
            @Qualifier("radiusBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            return BeanSupplier.of(MultifactorAuthenticationProvider.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val radius = casProperties.getAuthn().getMfa().getRadius();
                    val p = new RadiusMultifactorAuthenticationProvider(radiusTokenServers.toList());
                    p.setBypassEvaluator(radiusBypassEvaluator);
                    p.setFailureMode(radius.getFailureMode());
                    p.setFailureModeEvaluator(failureModeEvaluator);
                    p.setOrder(radius.getRank());
                    p.setId(radius.getId());
                    return p;
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusTokenAuthenticationEventExecutionPlanHandlerConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "radiusTokenAuthenticationHandler")
        public AuthenticationHandler radiusTokenAuthenticationHandler(
            @Qualifier("radiusMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenPrincipalFactory")
            final PrincipalFactory radiusTokenPrincipalFactory,
            @Qualifier("radiusTokenServers")
            final BeanContainer<RadiusServer> radiusTokenServers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val radius = casProperties.getAuthn().getMfa().getRadius();
                    return new RadiusTokenAuthenticationHandler(radius.getName(),
                        radiusTokenPrincipalFactory, radiusTokenServers.toList(),
                        radius.isFailoverOnException(),
                        radius.isFailoverOnAuthenticationFailure(),
                        radius.getOrder(), multifactorAuthenticationProvider);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusTokenAuthenticationEventExecutionPlanMetadataConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "radiusMultifactorProviderAuthenticationMetadataPopulator")
        public AuthenticationMetaDataPopulator radiusMultifactorProviderAuthenticationMetadataPopulator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                multifactorAuthenticationProvider, servicesManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "radiusAuthenticationMetaDataPopulator")
        public AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenAuthenticationHandler")
            final AuthenticationHandler radiusTokenAuthenticationHandler,
            @Qualifier("radiusMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider radiusMultifactorAuthenticationProvider) {
            return BeanSupplier.of(AuthenticationMetaDataPopulator.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val attribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
                    return new AuthenticationContextAttributeMetaDataPopulator(attribute, radiusTokenAuthenticationHandler,
                        radiusMultifactorAuthenticationProvider.getId());
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "RadiusTokenAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class RadiusTokenAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "radiusTokenAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer radiusTokenAuthenticationEventExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("radiusMultifactorProviderAuthenticationMetadataPopulator")
            final AuthenticationMetaDataPopulator radiusMultifactorProviderAuthenticationMetadataPopulator,
            final CasConfigurationProperties casProperties,
            @Qualifier("radiusTokenAuthenticationHandler")
            final AuthenticationHandler radiusTokenAuthenticationHandler,
            @Qualifier("radiusAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator radiusAuthenticationMetaDataPopulator) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val radius = casProperties.getAuthn().getMfa().getRadius();
                    val client = radius.getClient();
                    if (StringUtils.isNotBlank(client.getInetAddress())) {
                        plan.registerAuthenticationHandler(radiusTokenAuthenticationHandler);
                        plan.registerAuthenticationMetadataPopulator(radiusAuthenticationMetaDataPopulator);
                        plan.registerAuthenticationMetadataPopulator(radiusMultifactorProviderAuthenticationMetadataPopulator);
                        plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(RadiusTokenCredential.class));
                    }
                })
                .otherwiseProxy()
                .get();
        }
    }
}
