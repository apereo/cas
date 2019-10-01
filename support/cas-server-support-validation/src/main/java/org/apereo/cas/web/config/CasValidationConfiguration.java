package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ChainingCasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.ServiceValidationViewFactoryConfigurer;
import org.apereo.cas.web.ServiceValidationViewTypes;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.v1.LegacyValidateController;
import org.apereo.cas.web.v2.ProxyController;
import org.apereo.cas.web.v2.ProxyValidateController;
import org.apereo.cas.web.v2.ServiceValidateController;
import org.apereo.cas.web.v3.V3ProxyValidateController;
import org.apereo.cas.web.v3.V3ServiceValidateController;
import org.apereo.cas.web.view.Cas10ResponseView;
import org.apereo.cas.web.view.Cas20ResponseView;
import org.apereo.cas.web.view.Cas30ResponseView;
import org.apereo.cas.web.view.attributes.AttributeValuesPerLineProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.InlinedCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.View;

import java.util.ArrayList;

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
    private ObjectProvider<View> cas3SuccessView;

    @Autowired
    @Qualifier("authenticationAttributeReleasePolicy")
    private ObjectProvider<AuthenticationAttributeReleasePolicy> authenticationAttributeReleasePolicy;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas20ProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20ProtocolValidationSpecification;

    @Autowired
    @Qualifier("cas10ProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas10ProtocolValidationSpecification;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("cas2ServiceFailureView")
    private ObjectProvider<View> cas2ServiceFailureView;

    @Autowired
    @Qualifier("cas2SuccessView")
    private ObjectProvider<View> cas2SuccessView;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> serviceValidationAuthorizers;

    @Autowired
    @Qualifier("cas3ServiceFailureView")
    private ObjectProvider<View> cas3ServiceFailureView;

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
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedAuthenticationContextValidator> requestedContextValidator;

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
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas1ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
    public View cas1ServiceFailureView() {
        return new Cas10ResponseView(false,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas1ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
    public View cas2ServiceSuccessView() {
        return new Cas20ResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            cas2SuccessView.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            NoOpProtocolAttributesRenderer.INSTANCE);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
    public View cas3ServiceJsonView() {
        return new Cas30JsonResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
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
    @ConditionalOnMissingBean(name = "cas1ProtocolAttributesRenderer")
    public CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer() {
        switch (casProperties.getView().getCas1().getAttributeRendererType()) {
            case VALUES_PER_LINE:
                return new AttributeValuesPerLineProtocolAttributesRenderer();
            case DEFAULT:
            default:
                return NoOpProtocolAttributesRenderer.INSTANCE;
        }
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
    public View cas3ServiceSuccessView() {
        return new Cas30ResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            cas3SuccessView.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas3ProtocolAttributesRenderer());
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public ProxyController proxyController() {
        return new ProxyController(cas2ProxySuccessView.getObject(),
            cas2ProxyFailureView.getObject(),
            centralAuthenticationService.getObject(),
            webApplicationServiceFactory,
            applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidationViewFactory")
    public ServiceValidationViewFactory serviceValidationViewFactory() {
        val viewFactory = new ServiceValidationViewFactory();

        viewFactory.registerView(ServiceValidationViewTypes.JSON, cas3ServiceJsonView());

        val successViewV3 = cas3ServiceSuccessView();
        viewFactory.registerView(V3ServiceValidateController.class, Pair.of(successViewV3, cas3ServiceFailureView.getObject()));
        viewFactory.registerView(V3ProxyValidateController.class, Pair.of(successViewV3, cas3ServiceFailureView.getObject()));

        if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
            viewFactory.registerView(ProxyValidateController.class, Pair.of(successViewV3, cas3ServiceFailureView.getObject()));
            viewFactory.registerView(ServiceValidateController.class, Pair.of(successViewV3, cas3ServiceFailureView.getObject()));
        } else {
            val successViewV2 = cas2ServiceSuccessView();
            viewFactory.registerView(ProxyValidateController.class, Pair.of(successViewV2, cas2ServiceFailureView.getObject()));
            viewFactory.registerView(ServiceValidateController.class, Pair.of(successViewV2, cas2ServiceFailureView.getObject()));
        }

        viewFactory.registerView(LegacyValidateController.class, Pair.of(cas1ServiceSuccessView(), cas1ServiceFailureView()));

        val configurers = applicationContext.getBeansOfType(ServiceValidationViewFactoryConfigurer.class, false, true);
        val results = new ArrayList<ServiceValidationViewFactoryConfigurer>(configurers.values());
        AnnotationAwareOrderComparator.sort(results);
        results.forEach(cfg -> cfg.configureViewFactory(viewFactory));
        return viewFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ServiceValidateControllerValidationSpecification")
    public CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas20WithoutProxyProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ServiceValidateController")
    public V3ServiceValidateController v3ServiceValidateController() {
        val context = getServiceValidateConfigurationContextBuilder()
            .validationSpecifications(CollectionUtils.wrapSet(v3ServiceValidateControllerValidationSpecification()))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new V3ServiceValidateController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateControllerValidationSpecification")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas20ProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxyAuthnEnabled", havingValue = "true", matchIfMissing = true)
    public V3ProxyValidateController v3ProxyValidateController() {
        val context = getServiceValidateConfigurationContextBuilder()
            .validationSpecifications(CollectionUtils.wrapSet(v3ProxyValidateControllerValidationSpecification()))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new V3ProxyValidateController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyValidateControllerValidationSpecification")
    public CasProtocolValidationSpecification proxyValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas20ProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyValidateController")
    public ProxyValidateController proxyValidateController() {
        val context = getServiceValidateConfigurationContextBuilder()
            .validationSpecifications(CollectionUtils.wrapSet(proxyValidateControllerValidationSpecification()))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new ProxyValidateController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "legacyValidateControllerValidationSpecification")
    public CasProtocolValidationSpecification legacyValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas10ProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "legacyValidateController")
    public LegacyValidateController legacyValidateController() {
        val context = getServiceValidateConfigurationContextBuilder()
            .validationSpecifications(CollectionUtils.wrapSet(legacyValidateControllerValidationSpecification()))
            .proxyHandler(proxy10Handler.getObject())
            .build();
        return new LegacyValidateController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidateControllerValidationSpecification")
    public CasProtocolValidationSpecification serviceValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas20WithoutProxyProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceValidateController")
    public ServiceValidateController serviceValidateController() {
        val context = getServiceValidateConfigurationContextBuilder()
            .validationSpecifications(CollectionUtils.wrapSet(serviceValidateControllerValidationSpecification()))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new ServiceValidateController(context);
    }

    private ServiceValidateConfigurationContext.ServiceValidateConfigurationContextBuilder getServiceValidateConfigurationContextBuilder() {
        return ServiceValidateConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .servicesManager(servicesManager.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .argumentExtractor(argumentExtractor.getObject())
            .requestedContextValidator(requestedContextValidator.getObject())
            .authnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute())
            .validationAuthorizers(serviceValidationAuthorizers.getObject())
            .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
            .validationViewFactory(serviceValidationViewFactory());
    }
}
