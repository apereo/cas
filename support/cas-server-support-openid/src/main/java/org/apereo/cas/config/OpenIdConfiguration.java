package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceResponseBuilder;
import org.apereo.cas.support.openid.web.OpenIdProviderController;
import org.apereo.cas.support.openid.web.mvc.OpenIdValidateController;
import org.apereo.cas.support.openid.web.mvc.SmartOpenIdController;
import org.apereo.cas.support.openid.web.mvc.YadisController;
import org.apereo.cas.support.openid.web.support.OpenIdPostUrlHandlerMapping;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.CasProtocolViewFactory;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.DelegatingController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.ServiceValidationViewFactoryConfigurer;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.View;

import java.util.Properties;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated 6.2
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Deprecated(since = "6.2.0")
@Configuration(value = "openidConfiguration", proxyBeanMethods = false)
public class OpenIdConfiguration {

    @Bean
    public SmartOpenIdController smartOpenIdAssociationController(
        @Qualifier("serverManager")
        final ServerManager serverManager,
        @Qualifier("casOpenIdAssociationSuccessView")
        final View casOpenIdAssociationSuccessView) {
        return new SmartOpenIdController(serverManager, casOpenIdAssociationSuccessView);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "serverManager")
    @Autowired
    public ServerManager serverManager(final CasConfigurationProperties casProperties) {
        val manager = new ServerManager();
        manager.setOPEndpointUrl(casProperties.getServer().getLoginUrl());
        manager.setEnforceRpId(casProperties.getAuthn().getOpenid().isEnforceRpId());
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        LOGGER.trace("Creating openid server manager with OP endpoint [{}]", casProperties.getServer().getLoginUrl());
        return manager;
    }

    @ConditionalOnMissingBean(name = "openIdServiceResponseBuilder")
    @Bean
    @Autowired
    public ResponseBuilder openIdServiceResponseBuilder(final CasConfigurationProperties casProperties,
                                                        @Qualifier("serverManager")
                                                        final ServerManager serverManager,
                                                        @Qualifier("urlValidator")
                                                        final UrlValidator urlValidator,
                                                        @Qualifier(CentralAuthenticationService.BEAN_NAME)
                                                        final CentralAuthenticationService centralAuthenticationService,
                                                        @Qualifier(ServicesManager.BEAN_NAME)
                                                        final ServicesManager servicesManager) {
        val openIdPrefixUrl = casProperties.getServer().getPrefix().concat("/openid");
        return new OpenIdServiceResponseBuilder(openIdPrefixUrl, serverManager,
            centralAuthenticationService, servicesManager, urlValidator);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yadisController")
    public YadisController yadisController() {
        return new YadisController();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OpenIdProviderController openIdProviderController() {
        return new OpenIdProviderController();
    }

    @Bean
    @Autowired
    public OpenIdValidateController openIdValidateController(
        final CasConfigurationProperties casProperties,
        @Qualifier("serverManager")
        final ServerManager serverManager,
        @Qualifier("serviceValidationViewFactory")
        final ServiceValidationViewFactory serviceValidationViewFactory,
        @Qualifier("proxy20Handler")
        final ProxyHandler proxy20Handler,
        @Qualifier("argumentExtractor")
        final ArgumentExtractor argumentExtractor,
        @Qualifier(CentralAuthenticationService.BEAN_NAME)
        final CentralAuthenticationService centralAuthenticationService,
        @Qualifier("requestedContextValidator")
        final RequestedAuthenticationContextValidator requestedContextValidator,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
        final CasProtocolValidationSpecification cas20WithoutProxyProtocolValidationSpecification,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
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
            .validationViewFactory(serviceValidationViewFactory)
            .build();
        return new OpenIdValidateController(context, serverManager);
    }

    @Bean
    public OpenIdPostUrlHandlerMapping openIdPostUrlHandlerMapping(
        @Qualifier("smartOpenIdAssociationController")
        final SmartOpenIdController smartOpenIdAssociationController,
        @Qualifier("openIdValidateController")
        final OpenIdValidateController openIdValidateController) {
        val controller = new DelegatingController();
        controller.setDelegates(CollectionUtils.wrapList(smartOpenIdAssociationController, openIdValidateController));
        val m = new OpenIdPostUrlHandlerMapping();
        m.setOrder(1);
        val mappings = new Properties();
        mappings.put("/login", controller);
        m.setMappings(mappings);
        return m;
    }

    @Bean
    public ServiceValidationViewFactoryConfigurer openIdServiceValidationViewFactoryConfigurer(
        @Qualifier("casOpenIdServiceSuccessView")
        final View casOpenIdServiceSuccessView,
        @Qualifier("casOpenIdServiceFailureView")
        final View casOpenIdServiceFailureView) {
        return factory -> factory.registerView(OpenIdValidateController.class, Pair.of(casOpenIdServiceSuccessView, casOpenIdServiceFailureView));
    }

    /**
     * The openid protocol views.
     */
    @Configuration(value = "OpenIdProtocolViews", proxyBeanMethods = false)
    public static class OpenIdProtocolViews {

        @Bean
        @Autowired
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casOpenIdServiceFailureView(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory) {
            return casProtocolViewFactory.create(applicationContext, "protocol/openid/casOpenIdServiceFailureView");
        }

        @Bean
        @Autowired
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casOpenIdServiceSuccessView(final ConfigurableApplicationContext applicationContext,
                                                @Qualifier("casProtocolViewFactory")
                                                final CasProtocolViewFactory casProtocolViewFactory) {
            return casProtocolViewFactory.create(applicationContext, "protocol/openid/casOpenIdServiceSuccessView");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @Autowired
        public View casOpenIdAssociationSuccessView(
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory,
            final ConfigurableApplicationContext applicationContext) {
            return casProtocolViewFactory.create(applicationContext, "protocol/openid/casOpenIdAssociationSuccessView");
        }

        @Bean
        @Autowired
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View openIdProviderView(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casProtocolViewFactory")
            final CasProtocolViewFactory casProtocolViewFactory) {
            return casProtocolViewFactory.create(applicationContext, "protocol/openid/user");
        }
    }
}
