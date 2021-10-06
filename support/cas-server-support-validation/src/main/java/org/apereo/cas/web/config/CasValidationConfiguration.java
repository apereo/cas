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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import java.util.List;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casValidationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasValidationConfiguration {
    
    @Configuration(value = "CasValidationViewRegistrationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewRegistrationConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "casServiceValidationViewFactoryConfigurer")
        @Autowired
        public ServiceValidationViewFactoryConfigurer casServiceValidationViewFactoryConfigurer(
            final CasConfigurationProperties casProperties,
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
    }

    @Configuration(value = "CasValidationRendererConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationRendererConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "cas3ProtocolAttributesRenderer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer(final CasConfigurationProperties casProperties) {
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer(final CasConfigurationProperties casProperties) {
            switch (casProperties.getView().getCas1().getAttributeRendererType()) {
                case VALUES_PER_LINE:
                    return new AttributeValuesPerLineProtocolAttributesRenderer();
                case DEFAULT:
                default:
                    return NoOpProtocolAttributesRenderer.INSTANCE;
            }
        }

    }

    @Configuration(value = "CasValidationViewFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewFactoryConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "serviceValidationViewFactory")
        @Autowired
        public ServiceValidationViewFactory serviceValidationViewFactory(
            final List<ServiceValidationViewFactoryConfigurer> configurers) {
            val viewFactory = new ServiceValidationViewFactory();
            AnnotationAwareOrderComparator.sort(configurers);
            configurers.forEach(cfg -> cfg.configureViewFactory(viewFactory));
            return viewFactory;
        }
    }

    @Configuration(value = "CasValidationSpecificationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationSpecificationConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "v3ServiceValidateControllerValidationSpecification")
        @Autowired
        public CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification(
            @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(cas20WithoutProxyProtocolValidationSpecification);
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "v3ProxyValidateControllerValidationSpecification")
        @Autowired
        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        public CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification(
            @Qualifier("cas20ProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas20ProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(cas20ProtocolValidationSpecification);
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "proxyValidateControllerValidationSpecification")
        @Autowired
        public CasProtocolValidationSpecification proxyValidateControllerValidationSpecification(
            @Qualifier("cas20ProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas20ProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(cas20ProtocolValidationSpecification);
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "legacyValidateControllerValidationSpecification")
        @Autowired
        public CasProtocolValidationSpecification legacyValidateControllerValidationSpecification(
            @Qualifier("cas10ProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas10ProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(cas10ProtocolValidationSpecification);
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "serviceValidateControllerValidationSpecification")
        @Autowired
        public CasProtocolValidationSpecification serviceValidateControllerValidationSpecification(
            @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(cas20WithoutProxyProtocolValidationSpecification);
            return validationChain;
        }
    }

    @Configuration(value = "CasValidationViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public View cas3ServiceSuccessView(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("cas3ProtocolAttributesRenderer")
            final CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer,
            @Qualifier("cas3SuccessView")
            final View cas3SuccessView) {
            return new Cas30ResponseView(true, protocolAttributeEncoder, servicesManager,
                cas3SuccessView, authenticationAttributeReleasePolicy, authenticationServiceSelectionPlan,
                cas3ProtocolAttributesRenderer);
        }

        @Bean
        @Autowired
        public View cas2SuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getSuccess(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Autowired
        public View cas2ServiceFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getFailure());
        }

        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Autowired
        public View cas2ProxyFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getProxy().getFailure(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        @Bean
        @Autowired
        public View cas2ProxySuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getProxy().getSuccess(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Autowired
        public View cas3SuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas3().getSuccess());
        }

        @Bean
        @Autowired
        public View cas3ServiceFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas3().getFailure(),
                MediaType.APPLICATION_XML_VALUE);
        }

        @Bean
        @Autowired
        public View casPostResponseView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                "protocol/casPostResponseView");
        }

        @Bean
        @ConditionalOnMissingBean(name = "cas1ServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public View cas1ServiceSuccessView(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("cas1ProtocolAttributesRenderer")
            final CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer) {
            return new Cas10ResponseView(true, protocolAttributeEncoder, servicesManager,
                authenticationAttributeReleasePolicy, authenticationServiceSelectionPlan, cas1ProtocolAttributesRenderer);
        }

        @Bean
        @ConditionalOnMissingBean(name = "cas1ServiceFailureView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public View cas1ServiceFailureView(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("cas1ProtocolAttributesRenderer")
            final CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer) {
            return new Cas10ResponseView(false, protocolAttributeEncoder, servicesManager,
                authenticationAttributeReleasePolicy, authenticationServiceSelectionPlan, cas1ProtocolAttributesRenderer);
        }

        @Bean
        @ConditionalOnMissingBean(name = "cas2ServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public View cas2ServiceSuccessView(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("cas2SuccessView")
            final View cas2SuccessView) {
            return new Cas20ResponseView(true, protocolAttributeEncoder, servicesManager,
                cas2SuccessView, authenticationAttributeReleasePolicy, authenticationServiceSelectionPlan,
                NoOpProtocolAttributesRenderer.INSTANCE);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "cas3ServiceJsonView")
        @Autowired
        public View cas3ServiceJsonView(
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier("cas3ProtocolAttributesRenderer")
            final CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer) {
            return new Cas30JsonResponseView(true, protocolAttributeEncoder, servicesManager,
                authenticationAttributeReleasePolicy, authenticationServiceSelectionPlan, cas3ProtocolAttributesRenderer);
        }

    }

    @Configuration(value = "CasValidationControllerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationControllerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "proxyController")
        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        @Autowired
        public ProxyController proxyController(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("cas2ProxySuccessView")
            final View cas2ProxySuccessView,
            @Qualifier("cas2ProxyFailureView")
            final View cas2ProxyFailureView) {
            return new ProxyController(cas2ProxySuccessView, cas2ProxyFailureView,
                centralAuthenticationService, webApplicationServiceFactory, applicationContext);
        }



        @Bean
        @ConditionalOnMissingBean(name = "serviceValidateController")
        @Autowired
        public ServiceValidateController serviceValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            final CasConfigurationProperties casProperties,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("serviceValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification serviceValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(serviceValidationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(serviceValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new ServiceValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "legacyValidateController")
        @Autowired
        public LegacyValidateController legacyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("proxy10Handler")
            final ProxyHandler proxy10Handler,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("legacyValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification legacyValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(serviceValidationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(legacyValidateControllerValidationSpecification))
                .proxyHandler(proxy10Handler)
                .build();
            return new LegacyValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "proxyValidateController")
        @Autowired
        public ProxyValidateController proxyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("proxyValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification proxyValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(serviceValidationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(proxyValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new ProxyValidateController(context);
        }


        @Bean
        @ConditionalOnMissingBean(name = "v3ProxyValidateController")
        @ConditionalOnProperty(prefix = "cas.sso", name = "proxy-authn-enabled", havingValue = "true", matchIfMissing = true)
        @Autowired
        public V3ProxyValidateController v3ProxyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("v3ProxyValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(serviceValidationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(v3ProxyValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new V3ProxyValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "v3ServiceValidateController")
        @Autowired
        public V3ServiceValidateController v3ServiceValidateController(
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("v3ServiceValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(serviceValidationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(v3ServiceValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new V3ServiceValidateController(context);
        }
    }


}
