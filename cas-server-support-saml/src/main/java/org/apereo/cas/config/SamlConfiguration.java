package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.support.CasAttributeEncoder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.util.SamlCompliantUniqueTicketIdGenerator;
import org.apereo.cas.support.saml.web.SamlValidateController;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.ValidationSpecification;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SamlConfiguration} that creates the necessary opensaml context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("proxy20Handler")
    private ProxyHandler proxy20Handler;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ValidationSpecification cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier("serviceFactoryList")
    private List serviceFactoryList;

    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private Map uniqueIdGeneratorsMap;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List authenticationMetadataPopulators;

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
        view.setCasAttributeEncoder(casAttributeEncoder);
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
        view.setCasAttributeEncoder(casAttributeEncoder);
        return view;
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

    @Bean
    public SamlValidateController samlValidateController() {
        final SamlValidateController c = new SamlValidateController();
        c.setValidationSpecification(cas20WithoutProxyProtocolValidationSpecification);
        c.setSuccessView(casSamlServiceSuccessView());
        c.setFailureView(casSamlServiceFailureView());
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView);
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        serviceFactoryList.add(0, samlServiceFactory());
        uniqueIdGeneratorsMap.put(SamlService.class.getCanonicalName(), samlServiceTicketUniqueIdGenerator());
        authenticationMetadataPopulators.add(0, samlAuthenticationMetaDataPopulator());
    }
}
