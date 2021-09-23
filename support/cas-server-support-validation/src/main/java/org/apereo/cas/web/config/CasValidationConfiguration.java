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
import org.apereo.cas.validation.CasProtocolViewFactory;
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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.util.ArrayList;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casValidationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasValidationConfiguration {

    @Autowired
    @Qualifier("casProtocolViewFactory")
    private ObjectProvider<CasProtocolViewFactory> casProtocolViewFactory;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casAttributeEncoder")
    private ObjectProvider<ProtocolAttributeEncoder> protocolAttributeEncoder;

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
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> serviceValidationAuthorizers;

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
    @RefreshScope
    @Autowired
    public View cas1ServiceSuccessView(
        @Qualifier("cas1ProtocolAttributesRenderer")
        final CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer) {
        return new Cas10ResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas1ProtocolAttributesRenderer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
    @RefreshScope
    @Autowired
    public View cas1ServiceFailureView(
        @Qualifier("cas1ProtocolAttributesRenderer")
        final CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer) {
        return new Cas10ResponseView(false,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas1ProtocolAttributesRenderer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
    @RefreshScope
    @Autowired
    public View cas2ServiceSuccessView(
        @Qualifier("cas2SuccessView")
        final View cas2SuccessView) {
        return new Cas20ResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            cas2SuccessView,
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            NoOpProtocolAttributesRenderer.INSTANCE);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
    @Autowired
    public View cas3ServiceJsonView(
        @Qualifier("cas3ProtocolAttributesRenderer")
        final CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer) {
        return new Cas30JsonResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas3ProtocolAttributesRenderer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cas3ProtocolAttributesRenderer")
    @RefreshScope
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
    @RefreshScope
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
    @RefreshScope
    @Autowired
    public View cas3ServiceSuccessView(
        @Qualifier("cas3ProtocolAttributesRenderer")
        final CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer,
        @Qualifier("cas3SuccessView")
        final View cas3SuccessView) {
        return new Cas30ResponseView(true,
            protocolAttributeEncoder.getObject(),
            servicesManager.getObject(),
            cas3SuccessView,
            authenticationAttributeReleasePolicy.getObject(),
            authenticationServiceSelectionPlan.getObject(),
            cas3ProtocolAttributesRenderer);
    }

    @Bean
    @ConditionalOnMissingBean(name = "proxyController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    @Autowired
    public ProxyController proxyController(
        @Qualifier("cas2ProxySuccessView")
        final View cas2ProxySuccessView,
        @Qualifier("cas2ProxyFailureView")
        final View cas2ProxyFailureView) {
        return new ProxyController(cas2ProxySuccessView,
            cas2ProxyFailureView,
            centralAuthenticationService.getObject(),
            webApplicationServiceFactory,
            applicationContext);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "serviceValidationViewFactory")
    public ServiceValidationViewFactory serviceValidationViewFactory() {
        val viewFactory = new ServiceValidationViewFactory();
        val configurers = applicationContext.getBeansOfType(ServiceValidationViewFactoryConfigurer.class, false, true);
        val results = new ArrayList<>(configurers.values());
        AnnotationAwareOrderComparator.sort(results);
        results.forEach(cfg -> cfg.configureViewFactory(viewFactory));
        return viewFactory;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "casServiceValidationViewFactoryConfigurer")
    @Autowired
    public ServiceValidationViewFactoryConfigurer casServiceValidationViewFactoryConfigurer(
        @Qualifier("cas3ServiceSuccessView")
        final View cas3ServiceSuccessView,
        @Qualifier("cas3ServiceJsonView")
        final View cas3ServiceJsonView,
        @Qualifier("cas3ServiceFailureView")
        final View cas3ServiceFailureView,
        @Qualifier("cas2ServiceSuccessView")
        final View cas2ServiceSuccessView,
        @Qualifier("cas2ServiceFailureView")
        final View cas2ServiceFailureView,
        @Qualifier("cas1ServiceSuccessView")
        final View cas1ServiceSuccessView,
        @Qualifier("cas1ServiceFailureView")
        final View cas1ServiceFailureView) {
        return viewFactory -> {
            viewFactory.registerView(ServiceValidationViewTypes.JSON, cas3ServiceJsonView);

            viewFactory.registerView(V3ServiceValidateController.class, Pair.of(cas3ServiceSuccessView, cas3ServiceFailureView));
            viewFactory.registerView(V3ProxyValidateController.class, Pair.of(cas3ServiceSuccessView, cas3ServiceFailureView));

            if (casProperties.getView().getCas2().isV3ForwardCompatible()) {
                viewFactory.registerView(ProxyValidateController.class, Pair.of(cas3ServiceSuccessView, cas3ServiceFailureView));
                viewFactory.registerView(ServiceValidateController.class, Pair.of(cas3ServiceSuccessView, cas3ServiceFailureView));
            } else {
                viewFactory.registerView(ProxyValidateController.class, Pair.of(cas2ServiceSuccessView, cas2ServiceFailureView));
                viewFactory.registerView(ServiceValidateController.class, Pair.of(cas2ServiceSuccessView, cas2ServiceFailureView));
            }
            viewFactory.registerView(LegacyValidateController.class, Pair.of(cas1ServiceSuccessView, cas1ServiceFailureView));
        };
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
    @Autowired
    public V3ServiceValidateController v3ServiceValidateController(
        @Qualifier("v3ServiceValidateControllerValidationSpecification")
        final CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory) {
        val context = getServiceValidateConfigurationContextBuilder(serviceValidationViewFactory)
            .validationSpecifications(CollectionUtils.wrapSet(v3ServiceValidateControllerValidationSpecification))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new V3ServiceValidateController(context);
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateControllerValidationSpecification")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    public CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification() {
        val validationChain = new ChainingCasProtocolValidationSpecification();
        validationChain.addSpecification(cas20ProtocolValidationSpecification.getObject());
        return validationChain;
    }

    @Bean
    @ConditionalOnMissingBean(name = "v3ProxyValidateController")
    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    @Autowired
    public V3ProxyValidateController v3ProxyValidateController(
        @Qualifier("v3ProxyValidateControllerValidationSpecification")
        final CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory) {
        val context = getServiceValidateConfigurationContextBuilder(serviceValidationViewFactory)
            .validationSpecifications(CollectionUtils.wrapSet(v3ProxyValidateControllerValidationSpecification))
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
    @Autowired
    public ProxyValidateController proxyValidateController(
        @Qualifier("proxyValidateControllerValidationSpecification")
        final CasProtocolValidationSpecification proxyValidateControllerValidationSpecification,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory) {
        val context = getServiceValidateConfigurationContextBuilder(serviceValidationViewFactory)
            .validationSpecifications(CollectionUtils.wrapSet(proxyValidateControllerValidationSpecification))
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
    @Autowired
    public LegacyValidateController legacyValidateController(
        @Qualifier("legacyValidateControllerValidationSpecification")
        final CasProtocolValidationSpecification legacyValidateControllerValidationSpecification,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory) {
        val context = getServiceValidateConfigurationContextBuilder(serviceValidationViewFactory)
            .validationSpecifications(CollectionUtils.wrapSet(legacyValidateControllerValidationSpecification))
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
    @Autowired
    public ServiceValidateController serviceValidateController(
        @Qualifier("serviceValidateControllerValidationSpecification")
        final CasProtocolValidationSpecification serviceValidateControllerValidationSpecification,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory) {
        val context = getServiceValidateConfigurationContextBuilder(serviceValidationViewFactory)
            .validationSpecifications(CollectionUtils.wrapSet(serviceValidateControllerValidationSpecification))
            .proxyHandler(proxy20Handler.getObject())
            .build();
        return new ServiceValidateController(context);
    }

    @Bean
    public View cas2SuccessView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas2().getSuccess(),
            MediaType.APPLICATION_XML_VALUE);
    }

    @Bean
    public View cas2ServiceFailureView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas2().getFailure());
    }

    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public View cas2ProxyFailureView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas2().getProxy().getFailure(),
            MediaType.APPLICATION_XML_VALUE);
    }

    @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public View cas2ProxySuccessView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas2().getProxy().getSuccess(),
            MediaType.APPLICATION_XML_VALUE);
    }

    @Bean
    public View cas3SuccessView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas3().getSuccess());
    }

    @Bean
    public View cas3ServiceFailureView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            casProperties.getView().getCas3().getFailure(),
            MediaType.APPLICATION_XML_VALUE);
    }

    @Bean
    public View casPostResponseView() {
        return casProtocolViewFactory.getObject().create(applicationContext,
            "protocol/casPostResponseView");
    }

    private ServiceValidateConfigurationContext.ServiceValidateConfigurationContextBuilder
        getServiceValidateConfigurationContextBuilder(final ServiceValidationViewFactory serviceValidationViewFactory) {

        return ServiceValidateConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .servicesManager(servicesManager.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .argumentExtractor(argumentExtractor.getObject())
            .requestedContextValidator(requestedContextValidator.getObject())
            .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
            .validationAuthorizers(serviceValidationAuthorizers.getObject())
            .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
            .validationViewFactory(serviceValidationViewFactory);
    }
}
