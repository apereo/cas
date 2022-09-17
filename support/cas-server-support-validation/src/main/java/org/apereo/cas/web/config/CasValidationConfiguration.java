package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.CasProtocolVersionValidationSpecification;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.View;

import java.util.List;
import java.util.Set;

import static org.apereo.cas.validation.CasProtocolValidationSpecification.*;
import static org.springframework.http.MediaType.*;

/**
 * This is {@link CasValidationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Validation)
@AutoConfiguration
public class CasValidationConfiguration {
    private static final BeanCondition CONDITION_PROXY_AUTHN = BeanCondition.on("cas.sso.proxy-authn-enabled")
        .isTrue().evenIfMissing();

    @Configuration(value = "CasValidationViewRegistrationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewRegistrationConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "casServiceValidationViewFactoryConfigurer")
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
        public CasProtocolAttributesRenderer cas3ProtocolAttributesRenderer(final CasConfigurationProperties casProperties) {
            return switch (casProperties.getView().getCas3().getAttributeRendererType()) {
                case INLINE -> new InlinedCas30ProtocolAttributesRenderer();
                case DEFAULT -> new DefaultCas30ProtocolAttributesRenderer();
            };
        }

        @Bean
        @ConditionalOnMissingBean(name = "cas1ProtocolAttributesRenderer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolAttributesRenderer cas1ProtocolAttributesRenderer(final CasConfigurationProperties casProperties) {
            return switch (casProperties.getView().getCas1().getAttributeRendererType()) {
                case VALUES_PER_LINE -> new AttributeValuesPerLineProtocolAttributesRenderer();
                case DEFAULT -> NoOpProtocolAttributesRenderer.INSTANCE;
            };
        }

    }

    @Configuration(value = "CasValidationViewFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewFactoryConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "serviceValidationViewFactory")
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification(
            @Qualifier("casSingleAuthenticationProtocolValidationSpecification")
            final CasProtocolValidationSpecification casSingleAuthenticationProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(casSingleAuthenticationProtocolValidationSpecification);
            validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.CAS30)));
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "v3ProxyValidateControllerValidationSpecification")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casAlwaysSatisfiedProtocolValidationSpecification")
            final CasProtocolValidationSpecification casAlwaysSatisfiedProtocolValidationSpecification) throws Exception {
            return BeanSupplier.of(CasProtocolValidationSpecification.class)
                .when(CONDITION_PROXY_AUTHN.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val validationChain = new ChainingCasProtocolValidationSpecification();
                    validationChain.addSpecification(casAlwaysSatisfiedProtocolValidationSpecification);
                    validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.CAS30)));
                    return validationChain;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "proxyValidateControllerValidationSpecification")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification proxyValidateControllerValidationSpecification(
            @Qualifier("casAlwaysSatisfiedProtocolValidationSpecification")
            final CasProtocolValidationSpecification casAlwaysSatisfiedProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(casAlwaysSatisfiedProtocolValidationSpecification);
            validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.CAS20)));
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "legacyValidateControllerValidationSpecification")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification legacyValidateControllerValidationSpecification(
            @Qualifier("casSingleAuthenticationProtocolValidationSpecification")
            final CasProtocolValidationSpecification casSingleAuthenticationProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(casSingleAuthenticationProtocolValidationSpecification);
            validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.CAS10)));
            return validationChain;
        }

        @Bean
        @ConditionalOnMissingBean(name = "serviceValidateControllerValidationSpecification")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification serviceValidateControllerValidationSpecification(
            @Qualifier("casSingleAuthenticationProtocolValidationSpecification")
            final CasProtocolValidationSpecification casSingleAuthenticationProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(casSingleAuthenticationProtocolValidationSpecification);
            validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.CAS20)));
            return validationChain;
        }
    }

    @Configuration(value = "CasValidationViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasValidationViewsConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "cas3ServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas3ServiceSuccessView(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas2SuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getSuccess(),
                APPLICATION_XML_VALUE);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas2ServiceFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas2().getFailure());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas2ProxyFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(View.class)
                .when(CONDITION_PROXY_AUTHN.given(applicationContext.getEnvironment()))
                .supply(() -> casProtocolViewFactory.create(applicationContext,
                    casProperties.getView().getCas2().getProxy().getFailure(),
                    APPLICATION_XML_VALUE))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas2ProxySuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) throws Exception {
            return BeanSupplier.of(View.class)
                .when(CONDITION_PROXY_AUTHN.given(applicationContext.getEnvironment()))
                .supply(() -> casProtocolViewFactory.create(applicationContext,
                    casProperties.getView().getCas2().getProxy().getSuccess(),
                    APPLICATION_XML_VALUE))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas3SuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas3().getSuccess());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View cas3ServiceFailureView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext,
                casProperties.getView().getCas3().getFailure(),
                APPLICATION_XML_VALUE);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
        public View cas1ServiceSuccessView(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
        public View cas1ServiceFailureView(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
        public View cas2ServiceSuccessView(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
        public View cas3ServiceJsonView(
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProxyController proxyController(
            final CasConfigurationProperties casProperties,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("cas2ProxySuccessView")
            final View cas2ProxySuccessView,
            @Qualifier("cas2ProxyFailureView")
            final View cas2ProxyFailureView) {
            return new ProxyController(cas2ProxySuccessView, cas2ProxyFailureView,
                centralAuthenticationService, webApplicationServiceFactory,
                applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "serviceValidateController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("serviceValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification serviceValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .ticketRegistry(ticketRegistry)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .validationAuthorizers(serviceValidationAuthorizers)
                .casProperties(casProperties)
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(serviceValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new ServiceValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "legacyValidateController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LegacyValidateController legacyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("proxy10Handler")
            final ProxyHandler proxy10Handler,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
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
                .ticketRegistry(ticketRegistry)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .validationAuthorizers(serviceValidationAuthorizers)
                .casProperties(casProperties)
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(legacyValidateControllerValidationSpecification))
                .proxyHandler(proxy10Handler)
                .build();
            return new LegacyValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "proxyValidateController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ProxyValidateController proxyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("proxyValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification proxyValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .ticketRegistry(ticketRegistry)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .validationAuthorizers(serviceValidationAuthorizers)
                .casProperties(casProperties)
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(proxyValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new ProxyValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "v3ProxyValidateController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public V3ProxyValidateController v3ProxyValidateController(
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("v3ProxyValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification v3ProxyValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .ticketRegistry(ticketRegistry)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .validationAuthorizers(serviceValidationAuthorizers)
                .casProperties(casProperties)
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(v3ProxyValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new V3ProxyValidateController(context);
        }

        @Bean
        @ConditionalOnMissingBean(name = "v3ServiceValidateController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public V3ServiceValidateController v3ServiceValidateController(
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan serviceValidationAuthorizers,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier("v3ServiceValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification v3ServiceValidateControllerValidationSpecification,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory) {
            val context = ServiceValidateConfigurationContext.builder()
                .ticketRegistry(ticketRegistry)
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .requestedContextValidator(requestedContextValidator)
                .validationAuthorizers(serviceValidationAuthorizers)
                .casProperties(casProperties)
                .validationViewFactory(serviceValidationViewFactory)
                .validationSpecifications(CollectionUtils.wrapSet(v3ServiceValidateControllerValidationSpecification))
                .proxyHandler(proxy20Handler)
                .build();
            return new V3ServiceValidateController(context);
        }
    }

}
