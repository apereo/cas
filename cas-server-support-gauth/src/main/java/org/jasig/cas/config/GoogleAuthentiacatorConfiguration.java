package org.jasig.cas.config;

import org.jasig.cas.adaptors.gauth.GoogleAuthenticatorAccountRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GoogleAuthentiacatorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("googleAuthenticatorConfiguration")
public class GoogleAuthentiacatorConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("builder")
    private FlowBuilderServices builder;

    @Autowired
    @Qualifier("defaultGoogleAuthenticatorAccountRegistry")
    private GoogleAuthenticatorAccountRegistry googleAuthenticatorAccountRegistry;
    
    /**
     * Yubikey flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean(name = "googleAuthenticatorFlowRegistry")
    public FlowDefinitionRegistry googleAuthenticatorFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-gauth/*-webflow.xml");
        return builder.build();
    }

    /**
     * Google authenticator account registry google authenticator account registry.
     *
     * @return the google authenticator account registry
     */
    @RefreshScope
    @Bean(name="googleAuthenticatorAccountRegistry")
    public GoogleAuthenticatorAccountRegistry googleAuthenticatorAccountRegistry() {
        return this.googleAuthenticatorAccountRegistry;
    }
}
