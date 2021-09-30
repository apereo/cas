package org.apereo.cas.web.flow.config;

import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.X509CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.X509CertificateCredentialsRequestHeaderAction;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.tomcat.X509TomcatServletWebServiceFactoryCustomizer;
import org.apereo.cas.web.tomcat.X509TomcatServletWebServiceFactoryWebflowConfigurer;

import lombok.val;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "x509AuthenticationWebflowConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationWebflowConfiguration {

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer x509WebflowConfigurer(
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return new X509WebflowConfigurer(flowBuilderServices,
            loginFlowRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasMultifactorWebflowCustomizer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasMultifactorWebflowCustomizer x509CasMultifactorWebflowCustomizer() {
        return new X509CasMultifactorWebflowCustomizer();
    }

    @ConditionalOnMissingBean(name = "x509Check")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action x509Check(
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("x509CertificateExtractor")
        final X509CertificateExtractor x509CertificateExtractor,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final CasConfigurationProperties casProperties) {
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
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasWebflowExecutionPlanConfigurer")
    @Autowired
    public CasWebflowExecutionPlanConfigurer x509CasWebflowExecutionPlanConfigurer(
        @Qualifier("x509WebflowConfigurer")
        final CasWebflowConfigurer x509WebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(x509WebflowConfigurer);
    }

    @Configuration(value = "X509TomcatServletWebServiceFactoryConfiguration", proxyBeanMethods = false)
    @ConditionalOnClass(value = {Tomcat.class, Http2Protocol.class})
    @ConditionalOnProperty(value = "cas.authn.x509.webflow.port")
    public static class X509TomcatServletWebServiceFactoryConfiguration {
        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryCustomizer")
        @Bean
        @Autowired
        public ServletWebServerFactoryCustomizer x509TomcatServletWebServiceFactoryCustomizer(
            final ServerProperties serverProperties,
            final CasConfigurationProperties casProperties) {
            return new X509TomcatServletWebServiceFactoryCustomizer(serverProperties, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "x509TomcatWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer x509TomcatWebflowExecutionPlanConfigurer(
            @Qualifier("x509TomcatServletWebServiceFactoryWebflowConfigurer")
            final CasWebflowConfigurer x509TomcatServletWebServiceFactoryWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(x509TomcatServletWebServiceFactoryWebflowConfigurer);
        }

        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer x509TomcatServletWebServiceFactoryWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new X509TomcatServletWebServiceFactoryWebflowConfigurer(
                flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
        }
    }
}
