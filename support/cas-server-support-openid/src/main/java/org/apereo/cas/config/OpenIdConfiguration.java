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
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.web.servlet.View;

import java.util.Properties;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated 6.2
 */
@Configuration("openidConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Deprecated(since = "6.2.0")
public class OpenIdConfiguration {
    @Autowired
    @Qualifier("serviceValidationViewFactory")
    private ObjectProvider<ServiceValidationViewFactory> serviceValidationViewFactory;

    @Autowired
    @Qualifier("casOpenIdServiceSuccessView")
    private ObjectProvider<View> casOpenIdServiceSuccessView;

    @Autowired
    @Qualifier("casOpenIdServiceFailureView")
    private ObjectProvider<View> casOpenIdServiceFailureView;

    @Autowired
    @Qualifier("casOpenIdAssociationSuccessView")
    private ObjectProvider<View> casOpenIdAssociationSuccessView;

    @Autowired
    @Qualifier("proxy20Handler")
    private ObjectProvider<ProxyHandler> proxy20Handler;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedAuthenticationContextValidator> requestedContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ObjectProvider<CasProtocolValidationSpecification> cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> validationAuthorizers;

    @Bean
    public SmartOpenIdController smartOpenIdAssociationController() {
        return new SmartOpenIdController(serverManager(), casOpenIdAssociationSuccessView.getObject());
    }

    @RefreshScope
    @Bean
    public ServerManager serverManager() {
        val manager = new ServerManager();
        manager.setOPEndpointUrl(casProperties.getServer().getLoginUrl());
        manager.setEnforceRpId(casProperties.getAuthn().getOpenid().isEnforceRpId());
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        LOGGER.trace("Creating openid server manager with OP endpoint [{}]", casProperties.getServer().getLoginUrl());
        return manager;
    }

    @ConditionalOnMissingBean(name = "openIdServiceResponseBuilder")
    @Bean
    public ResponseBuilder openIdServiceResponseBuilder() {
        val openIdPrefixUrl = casProperties.getServer().getPrefix().concat("/openid");
        return new OpenIdServiceResponseBuilder(openIdPrefixUrl, serverManager(), centralAuthenticationService.getObject(), servicesManager.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yadisController")
    public YadisController yadisController() {
        return new YadisController();
    }

    @Bean
    @RefreshScope
    public OpenIdProviderController openIdProviderController() {
        return new OpenIdProviderController();
    }

    @Bean
    public OpenIdPostUrlHandlerMapping openIdPostUrlHandlerMapping() {
        val context = ServiceValidateConfigurationContext.builder()
            .validationSpecifications(CollectionUtils.wrapSet(cas20WithoutProxyProtocolValidationSpecification.getObject()))
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .servicesManager(servicesManager.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .argumentExtractor(argumentExtractor.getObject())
            .proxyHandler(proxy20Handler.getObject())
            .requestedContextValidator(requestedContextValidator.getObject())
            .authnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute())
            .validationAuthorizers(validationAuthorizers.getObject())
            .renewEnabled(casProperties.getSso().isRenewAuthnEnabled())
            .validationViewFactory(serviceValidationViewFactory.getObject())
            .build();

        val c = new OpenIdValidateController(context, serverManager());
        val controller = new DelegatingController();
        controller.setDelegates(CollectionUtils.wrapList(smartOpenIdAssociationController(), c));

        val m = new OpenIdPostUrlHandlerMapping();
        m.setOrder(1);
        val mappings = new Properties();
        mappings.put("/login", controller);
        m.setMappings(mappings);
        return m;
    }

    @Bean
    public ServiceValidationViewFactoryConfigurer openIdServiceValidationViewFactoryConfigurer() {
        return factory ->
            factory.registerView(OpenIdValidateController.class,
                Pair.of(casOpenIdServiceSuccessView.getObject(), casOpenIdServiceFailureView.getObject()));
    }


    /**
     * The openid protocol views.
     */
    @Configuration("OpenIdProtocolViews")
    public class OpenIdProtocolViews {

        @Autowired
        @Qualifier("casProtocolViewFactory")
        private ObjectProvider<CasProtocolViewFactory> casProtocolViewFactory;

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casOpenIdServiceFailureView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/openid/casOpenIdServiceFailureView");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casOpenIdServiceSuccessView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/openid/casOpenIdServiceSuccessView");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View casOpenIdAssociationSuccessView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/openid/casOpenIdAssociationSuccessView");
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public View openIdProviderView() {
            return casProtocolViewFactory.getObject().create(applicationContext, "protocol/openid/user");
        }

    }
}
