package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casValidationConfiguration")
public class CasValidationConfiguration {

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Resource(name = "defaultValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy defaultStrategy;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ValidationSpecification cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas20ProtocolValidationSpecification")
    private ValidationSpecification cas20ProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas10ProtocolValidationSpecification")
    private ValidationSpecification cas10ProtocolValidationSpecification;

    @javax.annotation.Resource(name = "webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas2ServiceFailureView")
    private View cas2ServiceFailureView;

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
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    /**
     * Validation service selection strategies list.
     *
     * @return the list
     */
    @Bean
    public List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies() {
        final List<ValidationServiceSelectionStrategy> list = new ArrayList<>();
        list.add(this.defaultStrategy);
        return list;
    }

    @Bean
    public View cas1ServiceSuccessView() {
        final Cas10ResponseView v = new Cas10ResponseView();
        v.setSuccessResponse(true);
        return v;
    }

    @Bean
    public View cas1ServiceFailureView() {
        final Cas10ResponseView v = new Cas10ResponseView();
        v.setSuccessResponse(false);
        return v;
    }

    @Bean
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView.Success();
    }

    @Bean
    public View cas3ServiceJsonView() {
        final Cas30JsonResponseView jsonResponseView = new Cas30JsonResponseView();
        jsonResponseView.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        jsonResponseView.setReleaseProtocolAttributes(casProperties.getView().getCas3().isReleaseProtocolAttributes());
        return jsonResponseView;
    }

    @Bean
    public View cas3ServiceSuccessView() {
        final Cas30ResponseView.Success s = new Cas30ResponseView.Success();
        s.setAuthenticationContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());
        s.setReleaseProtocolAttributes(casProperties.getView().getCas3().isReleaseProtocolAttributes());
        return s;
    }

    @Bean
    public V3ServiceValidateController v3ServiceValidateController() {
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

    @Bean
    public V3ProxyValidateController v3ProxyValidateController() {
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

    @Bean
    public ProxyValidateController proxyValidateController() {
        final ProxyValidateController c = new ProxyValidateController();
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

    @Bean
    public LegacyValidateController legacyValidateController() {
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
        final ProxyController c = new ProxyController();
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setWebApplicationServiceFactory(webApplicationServiceFactory);
        return c;
    }

    @Bean
    public ServiceValidateController serviceValidateController() {
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
