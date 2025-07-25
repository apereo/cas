package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.CasProtocolVersions;
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
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.CasProtocolVersionValidationSpecification;
import org.apereo.cas.validation.ChainingCasProtocolValidationSpecification;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactoryConfigurer;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.View;
import java.util.List;
import java.util.Set;

/**
 * This is {@link SamlConfiguration} that creates the necessary OpenSAML context and beans.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@Configuration(value = "SamlConfiguration", proxyBeanMethods = false)
class SamlConfiguration {

    @Configuration(value = "SamlViewFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlViewFactoryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
    static class SamlBuilderConfiguration {

        @ConditionalOnMissingBean(name = "samlResponseBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
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
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilder samlServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator) {
            return new SamlServiceResponseBuilder(servicesManager, urlValidator);
        }

        @ConditionalOnMissingBean(name = "saml10ObjectBuilder")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Saml10ObjectBuilder saml10ObjectBuilder(
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final OpenSamlConfigBean openSamlConfigBean) {
            return new Saml10ObjectBuilder(openSamlConfigBean);
        }

    }

    @Configuration(value = "SamlViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlViewsConfiguration {

        @ConditionalOnMissingBean(name = "casSamlServiceSuccessView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public View casSamlServiceSuccessView(
            @Qualifier("samlResponseBuilder")
            final SamlResponseBuilder samlResponseBuilder,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore) {
            return new Saml10SuccessResponseView(protocolAttributeEncoder, servicesManager,
                argumentExtractor, authenticationAttributeReleasePolicy,
                authenticationServiceSelectionPlan, NoOpProtocolAttributesRenderer.INSTANCE,
                samlResponseBuilder, attributeDefinitionStore);
        }

        @ConditionalOnMissingBean(name = "casSamlServiceFailureView")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public View casSamlServiceFailureView(
            @Qualifier("samlResponseBuilder")
            final SamlResponseBuilder samlResponseBuilder,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ArgumentExtractor argumentExtractor,
            @Qualifier("casAttributeEncoder")
            final ProtocolAttributeEncoder protocolAttributeEncoder,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME)
            final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore) {
            return new Saml10FailureResponseView(protocolAttributeEncoder, servicesManager,
                argumentExtractor, authenticationAttributeReleasePolicy,
                authenticationServiceSelectionPlan, NoOpProtocolAttributesRenderer.INSTANCE,
                samlResponseBuilder, attributeDefinitionStore);
        }
    }

    @Configuration(value = "SamlWebSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlWebSecurityConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebSecurityConfigurer<Void> samlProtocolEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {

                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(Strings.CI.prependIfMissing(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE, "/"));
                }
            };
        }
    }

    @Configuration(value = "SamlWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SamlWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlValidateEndpoint samlValidateEndpoint(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("samlResponseBuilder")
            final ObjectProvider<SamlResponseBuilder> samlResponseBuilder,
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory,
            @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
            final ObjectProvider<OpenSamlConfigBean> openSamlConfigBean,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final ObjectProvider<PrincipalResolver> defaultPrincipalResolver,
            @Qualifier(PrincipalFactory.BEAN_NAME)
            final ObjectProvider<PrincipalFactory> principalFactory) {
            return new SamlValidateEndpoint(casProperties, applicationContext, servicesManager,
                authenticationSystemSupport, webApplicationServiceFactory, principalFactory,
                samlResponseBuilder, openSamlConfigBean, registeredServiceAccessStrategyEnforcer,
                defaultPrincipalResolver);
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlValidateControllerValidationSpecification")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasProtocolValidationSpecification samlValidateControllerValidationSpecification(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier("casSingleAuthenticationProtocolValidationSpecification")
            final CasProtocolValidationSpecification casSingleAuthenticationProtocolValidationSpecification) {
            val validationChain = new ChainingCasProtocolValidationSpecification();
            validationChain.addSpecification(casSingleAuthenticationProtocolValidationSpecification);
            validationChain.addSpecification(new CasProtocolVersionValidationSpecification(Set.of(CasProtocolVersions.SAML1), tenantExtractor));
            return validationChain;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SamlValidateController samlValidateController(
            @Qualifier("casValidationConfigurationContext")
            final ServiceValidateConfigurationContext casValidationConfigurationContext,
            @Qualifier("proxy20Handler")
            final ProxyHandler proxy20Handler,
            @Qualifier("samlValidateControllerValidationSpecification")
            final CasProtocolValidationSpecification samlValidateControllerValidationSpecification) {
            return new SamlValidateController(casValidationConfigurationContext
                .withValidationSpecifications(CollectionUtils.wrapSet(samlValidateControllerValidationSpecification))
                .withProxyHandler(proxy20Handler));
        }

    }
}
