package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorTrustWebflowConfigurer;
import org.apereo.cas.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GoogleAuthenticatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthenticatorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class GoogleAuthenticatorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Bean
    public FlowDefinitionRegistry googleAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices.getObject());
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-gauth/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer() {
        return new GoogleAuthenticatorMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            googleAuthenticatorFlowRegistry(), applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer googleCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(googleAuthenticatorMultifactorWebflowConfigurer());
    }
    
    /**
     * The google authenticator multifactor trust configuration.
     */
    @ConditionalOnBean(name = "mfaTrustEngine")
    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("gauthMultifactorTrustConfiguration")
    public class GoogleAuthenticatorMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "gauthMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer() {
            return new GoogleAuthenticatorMultifactorTrustWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), googleAuthenticatorFlowRegistry(),
                applicationContext, casProperties);
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer gauthMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(gauthMultifactorTrustWebflowConfigurer());
        }
    }

}
