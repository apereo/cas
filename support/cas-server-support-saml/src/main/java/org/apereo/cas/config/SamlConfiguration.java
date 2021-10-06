package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceResponseBuilder;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.web.SamlValidateController;
import org.apereo.cas.support.saml.web.SamlValidateEndpoint;
import org.apereo.cas.support.saml.web.view.Saml10FailureResponseView;
import org.apereo.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.ServiceValidationViewFactoryConfigurer;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.View;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This is {@link SamlConfiguration} that creates the necessary OpenSAML context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlConfiguration", proxyBeanMethods = false)
public class SamlConfiguration {

    @Configuration(value = "SamlViewFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlViewFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlServiceValidationViewFactoryConfigurer")
        public ServiceValidationViewFactoryConfigurer samlServiceValidationViewFactoryConfigurer(
            @Qualifier("casSamlServiceSuccessView")
            final View casSamlServiceSuccessView,
            @Qualifier("casSamlServiceFailureView")
            final View casSamlServiceFailureView) {
            return factory -> factory.registerView(SamlValidateController.class, Pair.of(casSamlServiceSuccessView, casSamlServiceFailureView));
        }
    }

    @Configuration(value = "SamlBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlBuilderConfiguration {

        @ConditionalOnMissingBean(name = "samlResponseBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public SamlResponseBuilder samlResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("saml10ObjectBuilder")
            final Saml10ObjectBuilder saml10ObjectBuilder,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder) {
            val samlCore = casProperties.getSamlCore();
            return new SamlResponseBuilder(saml10ObjectBuilder, samlCore.getIssuer(),
                samlCore.getAttributeNamespace(), samlCore.getIssueLength(), samlCore.getSkewAllowance(),
                protocolAttributeEncoder, servicesManager);
        }

        @ConditionalOnMissingBean(name = "samlServiceResponseBuilder")
        @Bean
        public ResponseBuilder samlServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("urlValidator")
            final UrlValidator urlValidator) {
            return new SamlServiceResponseBuilder(servicesManager, urlValidator);
        }

        @ConditionalOnMissingBean(name = "saml10ObjectBuilder")
        @Bean
        @Autowired
        public Saml10ObjectBuilder saml10ObjectBuilder(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new Saml10ObjectBuilder(openSamlConfigBean);
        }

    }

    @Configuration(value = "SamlViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlViewsConfiguration {

        @ConditionalOnMissingBean(name = "casSamlServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public View casSamlServiceSuccessView(
            @Qualifier("samlResponseBuilder")
            final SamlResponseBuilder samlResponseBuilder,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            return new Saml10SuccessResponseView(protocolAttributeEncoder, servicesManager,
                argumentExtractor, StandardCharsets.UTF_8.name(), authenticationAttributeReleasePolicy,
                authenticationServiceSelectionPlan, NoOpProtocolAttributesRenderer.INSTANCE, samlResponseBuilder);
        }

        @ConditionalOnMissingBean(name = "casSamlServiceFailureView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public View casSamlServiceFailureView(
            @Qualifier("samlResponseBuilder")
            final SamlResponseBuilder samlResponseBuilder,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("authenticationAttributeReleasePolicy")
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy) {
            return new Saml10FailureResponseView(protocolAttributeEncoder, servicesManager,
                argumentExtractor, StandardCharsets.UTF_8.name(), authenticationAttributeReleasePolicy,
                authenticationServiceSelectionPlan, NoOpProtocolAttributesRenderer.INSTANCE, samlResponseBuilder);
        }
    }

    @Configuration(value = "SamlWebSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlWebSecurityConfiguration {
        @Bean
        public ProtocolEndpointWebSecurityConfigurer<Void> samlProtocolEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {

                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, "/"));
                }
            };
        }
    }

    @Configuration(value = "SamlWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public SamlValidateEndpoint samlValidateEndpoint(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlResponseBuilder")
            final SamlResponseBuilder samlResponseBuilder,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return new SamlValidateEndpoint(casProperties, servicesManager,
                authenticationSystemSupport, webApplicationServiceFactory, PrincipalFactoryUtils.newPrincipalFactory(),
                samlResponseBuilder, openSamlConfigBean, registeredServiceAccessStrategyEnforcer);
        }

        @Bean
        @Autowired
        public SamlValidateController samlValidateController(
            final CasConfigurationProperties casProperties,
            @Qualifier("serviceValidationViewFactory")
            final ServiceValidationViewFactory serviceValidationViewFactory,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
            final CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification,
            @Qualifier("serviceValidationAuthorizers")
            final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers) {
            val context = ServiceValidateConfigurationContext.builder()
                .validationSpecifications(CollectionUtils.wrapSet(cas20WithoutProxyProtocolValidationSpecification))
                .authenticationSystemSupport(authenticationSystemSupport)
                .servicesManager(servicesManager)
                .centralAuthenticationService(centralAuthenticationService)
                .argumentExtractor(argumentExtractor)
                .proxyHandler(proxy20Handler)
                .requestedContextValidator(requestedContextValidator)
                .authnContextAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute())
                .validationAuthorizers(validationAuthorizers)
                .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
                .validationViewFactory(serviceValidationViewFactory).build();
            return new SamlValidateController(context);
        }

    }
}
