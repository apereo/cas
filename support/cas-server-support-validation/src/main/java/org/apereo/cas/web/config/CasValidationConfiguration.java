package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.LegacyValidateController;
import org.apereo.cas.web.ProxyController;
import org.apereo.cas.web.ProxyValidateController;
import org.apereo.cas.web.ServiceValidateController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.v3.V3ProxyValidateController;
import org.apereo.cas.web.v3.V3ServiceValidateController;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.InlinedCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
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
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ObjectProvider<ProtocolAttributeEncoder> protocolAttributeEncoder;

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
    @Qualifier("serviceValidationAuthorizers")
    private ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers;

    @Autowired
    @Qualifier("cas3ServiceFailureView")
    private View cas3ServiceFailureView;

    @Autowired
    @Qualifier("cas2ProxySuccessView")
    private ObjectProvider<View> cas2ProxySuccessView;

    @Autowired
    @Qualifier("cas2ProxyFailureView")
    private ObjectProvider<View> cas2ProxyFailureView;

    @Autowired
    @Qualifier("proxy10Handler")
    private ObjectProvider<ProxyHandler> proxy10Handler;

    @Autowired
    @Qualifier("proxy20Handler")
    private ObjectProvider<ProxyHandler> proxy20Handler;

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
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceSuccessView")
    public View cas1ServiceSuccessView() {
        return new Cas10ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            authenticationAttributeReleasePolicy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView(false,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            authenticationAttributeReleasePolicy);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            cas2SuccessView,
            authenticationAttributeReleasePolicy,
            authenticationServiceSelectionPlan.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
    public View cas3ServiceJsonView() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        val isReleaseProtocolAttributes = casProperties.getAuthn().isReleaseProtocolAttributes();
        return new Cas30JsonResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager,
            authenticationContextAttribute,
            isReleaseProtocolAttributes,
            authenticationAttributeReleasePolicy,
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas3ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ProtocolAttributesRenderer")
    public CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer() {
        switch (casProperties.getView().getCas3().getAttributeRendererType()) {
            case INLINE:
                return new InlinedCas30ProtocolAttributesRenderer();
            case DEFAULT:
            default:
                return new DefaultCas30ProtocolAttributesRenderer();
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
    public View cas3ServiceSuccessView() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        val isReleaseProtocolAttributes = casProperties.getAuthn().isReleaseProtocolAttributes();
        return new Cas30ResponseView(true,
            protocolAttributeEncoder.getIfAvailable(),
            servicesManager,
            authenticationContextAttribute,
            cas3SuccessView,
            isReleaseProtocolAttributes,
            authenticationAttributeReleasePolicy,
            authenticationServiceSelectionPlan.getIfAvailable(),
            cas3ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public ProxyController proxyController() {
        return new ProxyController(cas2ProxySuccessView.getIfAvailable(),
            cas2ProxyFailureView.getIfAvailable(),
            centralAuthenticationService,
            webApplicationServiceFactory,
            applicationContext);
    }


    @Bean
    @ConditionalOnMissingBean(name = "v3ServiceValidateController")
    public V3ServiceValidateController v3ServiceValidateController() {
        return new V3ServiceValidateController(
            cas20WithoutProxyProtocolValidationSpecification,
            authenticationSystemSupport.getIfAvailable(),
            servicesManager,
            centralAuthenticationService,
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy,
            authenticationContextValidator,
            cas3ServiceJsonView(),
            cas3ServiceSuccessView(),
            cas3ServiceFailureView,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers,
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public V3ProxyValidateController v3ProxyValidateController() {
        return new V3ProxyValidateController(
            cas20ProtocolValidationSpecification,
            authenticationSystemSupport.getIfAvailable(),
            servicesManager,
            centralAuthenticationService,
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy,
            authenticationContextValidator,
            cas3ServiceJsonView(),
            cas3ServiceSuccessView(),
            cas3ServiceFailureView,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers,
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyValidateController")
    public ProxyValidateController proxyValidateController() {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ProxyValidateController(
                cas20ProtocolValidationSpecification,
                authenticationSystemSupport.getIfAvailable(),
                servicesManager,
                centralAuthenticationService,
                proxy20Handler.getIfAvailable(),
                argumentExtractor.getIfAvailable(),
                multifactorTriggerSelectionStrategy,
                authenticationContextValidator,
                cas3ServiceJsonView(),
                cas3ServiceSuccessView(),
                cas3ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                serviceValidationAuthorizers,
                casProperties.getSso().isRenewAuthnEnabled()
            );
        }

        return new ProxyValidateController(
            cas20ProtocolValidationSpecification,
            authenticationSystemSupport.getIfAvailable(),
            servicesManager,
            centralAuthenticationService,
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy,
            authenticationContextValidator,
            cas3ServiceJsonView(),
            cas2ServiceSuccessView(),
            cas2ServiceFailureView,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers,
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "legacyValidateController")
    public LegacyValidateController legacyValidateController() {
        return new LegacyValidateController(
            cas10ProtocolValidationSpecification,
            authenticationSystemSupport.getIfAvailable(),
            servicesManager,
            centralAuthenticationService,
            proxy10Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy,
            authenticationContextValidator,
            cas3ServiceJsonView(),
            cas1ServiceSuccessView(),
            cas1ServiceFailureView(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers,
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidateController")
    public ServiceValidateController serviceValidateController() {
        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            return new ServiceValidateController(
                cas20WithoutProxyProtocolValidationSpecification,
                authenticationSystemSupport.getIfAvailable(),
                servicesManager,
                centralAuthenticationService,
                proxy20Handler.getIfAvailable(),
                argumentExtractor.getIfAvailable(),
                multifactorTriggerSelectionStrategy,
                authenticationContextValidator,
                cas3ServiceJsonView(),
                cas3ServiceSuccessView(),
                cas3ServiceFailureView,
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                serviceValidationAuthorizers,
                casProperties.getSso().isRenewAuthnEnabled()
            );
        }

        return new ServiceValidateController(
            cas20WithoutProxyProtocolValidationSpecification,
            authenticationSystemSupport.getIfAvailable(),
            servicesManager,
            centralAuthenticationService,
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            multifactorTriggerSelectionStrategy,
            authenticationContextValidator,
            cas3ServiceJsonView(),
            cas2ServiceSuccessView(),
            cas2ServiceFailureView,
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serviceValidationAuthorizers,
            casProperties.getSso().isRenewAuthnEnabled()
        );
    }
}
