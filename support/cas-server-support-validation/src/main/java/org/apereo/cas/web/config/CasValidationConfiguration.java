package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationAuthorizer;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;

import java.util.LinkedHashSet;
import java.util.Set;

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
    @Qualifier("authenticationAttributeReleasePolicy")
    private AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas20ProtocolValidationSpecification")
    private CasProtocolValidationSpecification cas20ProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas10ProtocolValidationSpecification")
    private CasProtocolValidationSpecification cas10ProtocolValidationSpecification;

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
    @Qualifier("cas2ProxySuccessView")
    private View cas2ProxySuccessView;

    @Autowired
    @Qualifier("cas2ProxyFailureView")
    private View cas2ProxyFailureView;

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

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan selectionStrategies;
            
    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceSuccessView")
    public View cas1ServiceSuccessView() {
        return new Cas10ResponseView(true, protocolAttributeEncoder, servicesManager,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), authenticationAttributeReleasePolicy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView(false, protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                authenticationAttributeReleasePolicy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView(true, protocolAttributeEncoder,
                servicesManager, casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                cas2SuccessView, authenticationAttributeReleasePolicy, selectionStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
    public View cas3ServiceJsonView() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        final boolean isReleaseProtocolAttributes = casProperties.getAuthn().isReleaseProtocolAttributes();
        return new Cas30JsonResponseView(true,
                protocolAttributeEncoder,
                servicesManager,
                authenticationContextAttribute,
                isReleaseProtocolAttributes,
                authenticationAttributeReleasePolicy,
                selectionStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
    public View cas3ServiceSuccessView() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        final boolean isReleaseProtocolAttributes = casProperties.getAuthn().isReleaseProtocolAttributes();
        return new Cas30ResponseView(true,
                protocolAttributeEncoder,
                servicesManager,
                authenticationContextAttribute,
                cas3SuccessView,
                isReleaseProtocolAttributes,
                authenticationAttributeReleasePolicy,
                selectionStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyController")
    public ProxyController proxyController() {
        return new ProxyController(centralAuthenticationService, webApplicationServiceFactory,
                cas2ProxySuccessView, cas2ProxyFailureView);
    }
    
    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "v3ServiceValidateController")
    public V3ServiceValidateController v3ServiceValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor,
                                                                   @Qualifier("defaultAuthenticationSystemSupport") 
                                                                   final AuthenticationSystemSupport authenticationSystemSupport) {
        return new V3ServiceValidateController(
                cas20WithoutProxyProtocolValidationSpecification, authenticationSystemSupport,
                servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                cas3ServiceJsonView(), cas3ServiceSuccessView(), cas3ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
        );
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateController")
    public V3ProxyValidateController v3ProxyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor,
                                                               @Qualifier("defaultAuthenticationSystemSupport") 
                                                               final AuthenticationSystemSupport authenticationSystemSupport) {
        return new V3ProxyValidateController(
                cas20ProtocolValidationSpecification, authenticationSystemSupport,
                servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                cas3ServiceJsonView(), cas3ServiceSuccessView(), cas3ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
        );
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "proxyValidateController")
    public ProxyValidateController proxyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor,
                                                           @Qualifier("defaultAuthenticationSystemSupport") 
                                                           final AuthenticationSystemSupport authenticationSystemSupport) {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ProxyValidateController(
                    cas20ProtocolValidationSpecification, authenticationSystemSupport,
                    servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                    multifactorTriggerSelectionStrategy, authenticationContextValidator,
                    cas3ServiceJsonView(), cas3ServiceSuccessView(), cas3ServiceFailureView,
                    casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
            );
        }

        return new ProxyValidateController(
                cas20ProtocolValidationSpecification, authenticationSystemSupport,
                servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                cas3ServiceJsonView(), cas2ServiceSuccessView(), cas2ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
        );
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "legacyValidateController")
    public LegacyValidateController legacyValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor,
                                                             @Qualifier("defaultAuthenticationSystemSupport") 
                                                             final AuthenticationSystemSupport authenticationSystemSupport) {
        return new LegacyValidateController(
                cas10ProtocolValidationSpecification, authenticationSystemSupport,
                servicesManager, centralAuthenticationService, proxy10Handler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                cas3ServiceJsonView(), cas1ServiceSuccessView(), cas1ServiceFailureView(),
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
        );
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "serviceValidateController")
    public ServiceValidateController serviceValidateController(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor,
                                                               @Qualifier("defaultAuthenticationSystemSupport") 
                                                               final AuthenticationSystemSupport authenticationSystemSupport) {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ServiceValidateController(
                    cas20WithoutProxyProtocolValidationSpecification, authenticationSystemSupport,
                    servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                    multifactorTriggerSelectionStrategy, authenticationContextValidator,
                    cas3ServiceJsonView(), cas3ServiceSuccessView(), cas3ServiceFailureView,
                    casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
            );
        }

        return new ServiceValidateController(
                cas20WithoutProxyProtocolValidationSpecification, authenticationSystemSupport,
                servicesManager, centralAuthenticationService, proxy20Handler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                cas3ServiceJsonView(), cas2ServiceSuccessView(), cas2ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(), serviceValidationAuthorizers()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidationAuthorizers")
    public Set<ValidationAuthorizer> serviceValidationAuthorizers() {
        return new LinkedHashSet<>(0);
    }
}
