package org.apereo.cas.config;

import org.apereo.cas.authentication.support.CasAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlConfiguration} that creates the necessary opensaml context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlConfiguration")
public class SamlConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

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
        view.setIssuer(casProperties.getSamlResponse().getIssuer());
        view.setSkewAllowance(casProperties.getSamlResponse().getSkewAllowance());
        view.setDefaultAttributeNamespace(casProperties.getSamlResponse().getAttributeNamespace());
        view.setSamlObjectBuilder(saml10ObjectBuilder());
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
        view.setSamlObjectBuilder(saml10ObjectBuilder());
        return view;
    }

    @Bean
    public BaseApplicationContextWrapper samlApplicationContextWrapper() {
        final SamlApplicationContextWrapper w = new SamlApplicationContextWrapper();
        w.setSamlAuthenticationMetaDataPopulator(samlAuthenticationMetaDataPopulator());
        w.setSamlServiceFactory(samlServiceFactory());
        w.setSamlServiceTicketUniqueIdGenerator(samlServiceTicketUniqueIdGenerator());
        return w;
    }

    @Bean
    public SamlAuthenticationMetaDataPopulator samlAuthenticationMetaDataPopulator() {
        return new SamlAuthenticationMetaDataPopulator();
    }

    @Bean
    public SamlServiceFactory samlServiceFactory() {
        return new SamlServiceFactory();
    }

    @Bean
    public Saml10ObjectBuilder saml10ObjectBuilder() {
        final Saml10ObjectBuilder w = new Saml10ObjectBuilder();
        w.setConfigBean(this.configBean);
        return w;
    }

    @Bean
    public SamlCompliantUniqueTicketIdGenerator samlServiceTicketUniqueIdGenerator() {
        final SamlCompliantUniqueTicketIdGenerator gen =
                new SamlCompliantUniqueTicketIdGenerator(casProperties.getServer().getName());
        gen.setSaml2compliant(casProperties.getSamlResponse().isTicketidSaml2());
        return gen;
    }

}
