package org.apereo.cas.support.pac4j.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.Pac4jApplicationContextWrapper;
import org.apereo.cas.support.pac4j.Pac4jProperties;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.apereo.cas.support.pac4j.web.flow.ClientAction;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link Pac4jConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jConfiguration")
public class Pac4jConfiguration {
    
    @Bean
    public Pac4jProperties pac4jProperties() {
        return new Pac4jProperties();
    }
    
    @Bean
    public BaseApplicationContextWrapper pac4jApplicationContextWrapper() {
        return new Pac4jApplicationContextWrapper();
    }
    
    @Bean
    public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
        return new ClientAuthenticationMetaDataPopulator();
    }
    
    @Bean
    public AuthenticationHandler clientAuthenticationHandler() {
        return new ClientAuthenticationHandler();
    }

    @Bean
    public Action clientAction() {
        return new ClientAction();
    }
}
