package org.apereo.cas.config;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAccountRegistry;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorApplicationContextWrapper;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorInstance;
import org.apereo.cas.adaptors.gauth.GoogleAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.gauth.InMemoryGoogleAuthenticatorAccountRegistry;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.web.BaseApplicationContextWrapper;
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
    @Bean
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
    @Bean
    public GoogleAuthenticatorAccountRegistry googleAuthenticatorAccountRegistry() {
        return this.googleAuthenticatorAccountRegistry;
    }
    
    @Bean
    public BaseApplicationContextWrapper googleAuthenticatorApplicationContextWrapper() {
        return new GoogleAuthenticatorApplicationContextWrapper();
    }
    
    @Bean
    public AuthenticationHandler googleAuthenticatorAuthenticationHandler() {
        return new GoogleAuthenticatorAuthenticationHandler();
    }
    
    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator googleAuthenticatorAuthenticationMetaDataPopulator() {
        return new GoogleAuthenticatorAuthenticationMetaDataPopulator();
    }
    
    @Bean
    @RefreshScope
    public IGoogleAuthenticator googleAuthenticatorInstance() {
        return new GoogleAuthenticatorInstance();
    }
    
    @Bean
    @RefreshScope
    public AbstractMultifactorAuthenticationProvider googleAuthenticatorAuthenticationProvider() {
        return new GoogleAuthenticatorMultifactorAuthenticationProvider();
    }
    
    @Bean
    public GoogleAuthenticatorAccountRegistry defaultGoogleAuthenticatorAccountRegistry() {
        return new InMemoryGoogleAuthenticatorAccountRegistry();
    }
}
