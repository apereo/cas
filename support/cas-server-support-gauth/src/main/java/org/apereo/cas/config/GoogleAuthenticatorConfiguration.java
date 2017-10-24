package org.apereo.cas.config;

import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;
    
    @Bean
    public FlowDefinitionRegistry googleAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-gauth/*-webflow.xml");
        return builder.build();
    }
    
    @ConditionalOnMissingBean(name = "googleAuthenticatorMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer googleAuthenticatorMultifactorWebflowConfigurer() {
        final CasWebflowConfigurer w = new GoogleAuthenticatorMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                googleAuthenticatorFlowRegistry(), applicationContext, casProperties);
        w.initialize();
        return w;
    }
    
    /**
     * The google authenticator multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.gauth", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("gauthMultifactorTrustConfiguration")
    public class GoogleAuthenticatorMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "gauthMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer gauthMultifactorTrustWebflowConfigurer() {
            final CasWebflowConfigurer w = new GoogleAuthenticatorMultifactorTrustWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                    casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), googleAuthenticatorFlowRegistry(), 
                    applicationContext, casProperties);
            
            w.initialize();
            return w;
        }
    }
    
}
