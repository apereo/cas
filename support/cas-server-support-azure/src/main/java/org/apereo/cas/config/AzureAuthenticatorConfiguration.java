package org.apereo.cas.config;


import org.apereo.cas.adaptors.azure.web.flow.AzureAuthenticatorMultifactorTrustWebflowConfigurer;
import org.apereo.cas.adaptors.azure.web.flow.AzureAuthenticatorMultifactorWebflowConfigurer;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link AzureAuthenticatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("azureAuthenticatorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class AzureAuthenticatorConfiguration {

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
    public FlowDefinitionRegistry azureAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-azure/*-webflow.xml");
        return builder.build();
    }
    
    @ConditionalOnMissingBean(name = "azureAuthenticatorMultifactorWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer azureAuthenticatorMultifactorWebflowConfigurer() {
        return new AzureAuthenticatorMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                azureAuthenticatorFlowRegistry());
    }
    
    /**
     * The azure authenticator multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.azure", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("azureMultifactorTrustConfiguration")
    public class AzureAuthenticatorMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "azureMultifactorTrustWebflowConfigurer")
        @Bean
        public CasWebflowConfigurer azureMultifactorTrustWebflowConfigurer() {
            return new AzureAuthenticatorMultifactorTrustWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                    casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), loginFlowDefinitionRegistry);
        }
    }
    
}
