package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.ValidationSpecification;
import org.apereo.cas.web.LegacyValidateController;
import org.apereo.cas.web.ProxyController;
import org.apereo.cas.web.ProxyValidateController;
import org.apereo.cas.web.ServiceValidateController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.v3.V3ProxyValidateController;
import org.apereo.cas.web.v3.V3ServiceValidateController;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30JsonResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casValidationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasValidationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ProtocolAttributeEncoder protocolAttributeEncoder;

    @Autowired
    @Qualifier("cas3SuccessView")
    private View cas3SuccessView;

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
    @Qualifier("cas20ProtocolValidationSpecification")
    private ValidationSpecification cas20ProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas10ProtocolValidationSpecification")
    private ValidationSpecification cas10ProtocolValidationSpecification;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas2ServiceFailureView")
    private View cas2ServiceFailureView;

    @Autowired
    @Qualifier("cas2SuccessView")
    private View cas2SuccessView;

    @Autowired
    @Qualifier("cas3ServiceFailureView")
    private View cas3ServiceFailureView;

    @Autowired
    @Qualifier("proxy10Handler")
    private ProxyHandler proxy10Handler;

    @Autowired
    @Qualifier("proxy20Handler")
    private ProxyHandler proxy20Handler;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;
    
    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Bean
    public View cas1ServiceSuccessView() {
        return new Cas10ResponseView(true, protocolAttributeEncoder, servicesManager,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
    }

    @Bean
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView(false, protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
    }

    @Bean
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView(true, protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                this.cas2SuccessView);
    }

    @Bean
    public View cas3ServiceJsonView() {
        return new Cas30JsonResponseView(true, 
                protocolAttributeEncoder,
                servicesManager,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                casProperties.getAuthn().isReleaseProtocolAttributes());
    }

    @Bean
    public View cas3ServiceSuccessView() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        final boolean isReleaseProtocolAttributes = casProperties.getAuthn().isReleaseProtocolAttributes();
        return new Cas30ResponseView(true, protocolAttributeEncoder,
                servicesManager, authenticationContextAttribute, cas3SuccessView, isReleaseProtocolAttributes);
    }

    @Autowired
    @Bean
    public V3ServiceValidateController v3ServiceValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        final V3ServiceValidateController c = new V3ServiceValidateController();
        c.setValidationSpecification(this.cas20WithoutProxyProtocolValidationSpecification);
        c.setSuccessView(cas3ServiceSuccessView());
        c.setFailureView(cas3ServiceFailureView);
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView());
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }

    @Autowired
    @Bean
    public V3ProxyValidateController v3ProxyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        final V3ProxyValidateController c = new V3ProxyValidateController();
        c.setValidationSpecification(cas20ProtocolValidationSpecification);
        c.setSuccessView(cas3ServiceSuccessView());
        c.setFailureView(cas3ServiceFailureView);
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView());
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }

    @Autowired
    @Bean
    public ProxyValidateController proxyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        final ProxyValidateController c = new ProxyValidateController();
        c.setValidationSpecification(cas20ProtocolValidationSpecification);
        c.setSuccessView(cas3ServiceSuccessView());
        c.setFailureView(cas3ServiceFailureView);
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView());
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }

    @Autowired
    @Bean
    public LegacyValidateController legacyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        final LegacyValidateController c = new LegacyValidateController();
        c.setValidationSpecification(this.cas10ProtocolValidationSpecification);
        c.setSuccessView(cas1ServiceSuccessView());
        c.setFailureView(cas1ServiceFailureView());
        c.setProxyHandler(proxy10Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView());
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }

    @Bean
    public ProxyController proxyController() {
        return new ProxyController(centralAuthenticationService, webApplicationServiceFactory);
    }

    @Autowired
    @Bean
    public ServiceValidateController serviceValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        final ServiceValidateController c = new ServiceValidateController();
        c.setValidationSpecification(this.cas20WithoutProxyProtocolValidationSpecification);
        c.setSuccessView(cas2ServiceSuccessView());
        c.setFailureView(cas2ServiceFailureView);
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView());
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        return c;
    }
}
