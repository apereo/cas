package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.support.CasAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.SamlApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * This is {@link SamlConfiguration} that creates the necessary opensaml context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlConfiguration")
public class SamlConfiguration {
    
    @Value("${cas.saml.response.issuer:localhost}")
    private String issuer;
    
    @Value("${cas.saml.response.skewAllowance:0}")
    private int skewAllowance;

    @Value("${server.name}") 
    private String serverName;
    
    @Resource(name="servicesManager")
    private ServicesManager servicesManager;


    @Resource(name="casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

    @Value("${cas.saml.attribute.namespace:http://www.ja-sig.org/products/cas/}")
    private String defaultAttributeNamespace;

    /**
     * Cas saml service success view saml 10 success response view.
     *
     * @return the saml 10 success response view
     */
    @RefreshScope
    @Bean
    public Saml10SuccessResponseView casSamlServiceSuccessView() {
        final Saml10SuccessResponseView view = new Saml10SuccessResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        view.setIssuer(this.issuer);
        view.setSkewAllowance(this.skewAllowance);
        view.setDefaultAttributeNamespace(this.defaultAttributeNamespace);
        return view;
    }

    /**
     * Cas saml service failure view saml 10 failure response view.
     *
     * @return the saml 10 failure response view
     */
    @RefreshScope
    @Bean
    public Saml10FailureResponseView casSamlServiceFailureView() {
        final Saml10FailureResponseView view = new Saml10FailureResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        return view;
    }
    
    @Bean
    public BaseApplicationContextWrapper samlApplicationContextWrapper() {
        return new SamlApplicationContextWrapper();
    }

    @Bean
    public AuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator() {
        return new SamlAuthenticationMetaDataPopulator();
    }

    @Bean
    public ServiceFactory<SamlService> samlServiceFactory() {
        return new SamlServiceFactory();
    }

    @Bean
    public Saml10ObjectBuilder saml10ObjectBuilder() {
        return new Saml10ObjectBuilder();
    }

    @Bean
    public SamlCompliantUniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator() {
        return new SamlCompliantUniqueTicketIdGenerator(this.serverName);
    }
    
}
