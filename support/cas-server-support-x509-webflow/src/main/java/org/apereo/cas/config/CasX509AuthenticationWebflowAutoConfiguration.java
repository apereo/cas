package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.X509CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.X509CertificateCredentialsRequestHeaderAction;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.tomcat.X509TomcatServletWebServiceFactoryCustomizer;
import org.apereo.cas.web.tomcat.X509TomcatServletWebServiceFactoryWebflowConfigurer;
import lombok.val;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasX509AuthenticationWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.X509)
@AutoConfiguration
public class CasX509AuthenticationWebflowAutoConfiguration {

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer x509WebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new X509WebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasMultifactorWebflowCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasMultifactorWebflowCustomizer x509CasMultifactorWebflowCustomizer() {
        return new X509CasMultifactorWebflowCustomizer();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_X509_CHECK)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action x509Check(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("x509CertificateExtractor")
        final X509CertificateExtractor x509CertificateExtractor,
        @Qualifier(CasWebflowEventResolver.BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER)
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier(AdaptiveAuthenticationPolicy.BEAN_NAME)
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> {
                val extractCertFromRequestHeader = casProperties.getAuthn().getX509().isExtractCert();
                if (extractCertFromRequestHeader) {
                    return new X509CertificateCredentialsRequestHeaderAction(
                        initialAuthenticationAttemptWebflowEventResolver,
                        serviceTicketRequestWebflowEventResolver,
                        adaptiveAuthenticationPolicy,
                        x509CertificateExtractor, casProperties);
                }
                return new X509CertificateCredentialsNonInteractiveAction(
                    initialAuthenticationAttemptWebflowEventResolver,
                    serviceTicketRequestWebflowEventResolver,
                    adaptiveAuthenticationPolicy, casProperties);
            })
            .withId(CasWebflowConstants.ACTION_ID_X509_CHECK)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "x509CasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer x509CasWebflowExecutionPlanConfigurer(
        @Qualifier("x509WebflowConfigurer")
        final CasWebflowConfigurer x509WebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(x509WebflowConfigurer);
    }

    @SuppressWarnings("ConditionalOnProperty")
    @Configuration(value = "X509TomcatServletWebServiceFactoryConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass({Tomcat.class, Http2Protocol.class})
    @ConditionalOnProperty("cas.authn.x509.webflow.port")
    static class X509TomcatServletWebServiceFactoryConfiguration {
        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryCustomizer")
        @Bean
        public WebServerFactoryCustomizer x509TomcatServletWebServiceFactoryCustomizer(
            final ServerProperties serverProperties,
            final TomcatServerProperties tomcatServerProperties,
            final CasConfigurationProperties casProperties) {
            return new X509TomcatServletWebServiceFactoryCustomizer(serverProperties, tomcatServerProperties, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "x509TomcatWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer x509TomcatWebflowExecutionPlanConfigurer(
            @Qualifier("x509TomcatServletWebServiceFactoryWebflowConfigurer")
            final CasWebflowConfigurer x509TomcatServletWebServiceFactoryWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(x509TomcatServletWebServiceFactoryWebflowConfigurer);
        }

        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer x509TomcatServletWebServiceFactoryWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new X509TomcatServletWebServiceFactoryWebflowConfigurer(
                flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        }
    }
}
