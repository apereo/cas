package org.apereo.cas.web.flow.config;

import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationWebflowConfiguration {
    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("x509CertificateExtractor")
    private ObjectProvider<X509CertificateExtractor> x509CertificateExtractor;

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer x509WebflowConfigurer() {
        return new X509WebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasMultifactorWebflowCustomizer")
    @RefreshScope
    public CasMultifactorWebflowCustomizer x509CasMultifactorWebflowCustomizer() {
        return new X509CasMultifactorWebflowCustomizer();
    }
    
    @ConditionalOnMissingBean(name = "x509Check")
    @Bean
    @RefreshScope
    public Action x509Check() {
        val extractCertFromRequestHeader = casProperties.getAuthn().getX509().isExtractCert();
        if (extractCertFromRequestHeader) {
            return new X509CertificateCredentialsRequestHeaderAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
                serviceTicketRequestWebflowEventResolver.getObject(),
                adaptiveAuthenticationPolicy.getObject(),
                x509CertificateExtractor.getObject(), casProperties);
        }
        return new X509CertificateCredentialsNonInteractiveAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer x509CasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(x509WebflowConfigurer());
    }

    @Configuration("X509TomcatServletWebServiceFactoryConfiguration")
    @ConditionalOnClass(value = {Tomcat.class, Http2Protocol.class})
    @ConditionalOnProperty(value = "cas.authn.x509.webflow.port")
    public class X509TomcatServletWebServiceFactoryConfiguration {
        @Autowired
        private ServerProperties serverProperties;

        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryCustomizer")
        @Bean
        public ServletWebServerFactoryCustomizer x509TomcatServletWebServiceFactoryCustomizer() {
            return new X509TomcatServletWebServiceFactoryCustomizer(serverProperties, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "x509TomcatWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer x509TomcatWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(x509TomcatServletWebServiceFactoryWebflowConfigurer());
        }

        @ConditionalOnMissingBean(name = "x509TomcatServletWebServiceFactoryWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer x509TomcatServletWebServiceFactoryWebflowConfigurer() {
            return new X509TomcatServletWebServiceFactoryWebflowConfigurer(
                flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                applicationContext, casProperties);
        }
    }
}
