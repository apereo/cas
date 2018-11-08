package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceResponseBuilder;
import org.apereo.cas.support.openid.web.OpenIdProviderController;
import org.apereo.cas.support.openid.web.flow.OpenIdSingleSignOnAction;
import org.apereo.cas.support.openid.web.mvc.OpenIdValidateController;
import org.apereo.cas.support.openid.web.mvc.SmartOpenIdController;
import org.apereo.cas.support.openid.web.mvc.YadisController;
import org.apereo.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.apereo.cas.support.openid.web.support.OpenIdPostUrlHandlerMapping;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.AbstractDelegateController;
import org.apereo.cas.web.DelegatingController;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.Action;

import java.util.Properties;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("openidConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OpenIdConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private ObjectProvider<View> cas3ServiceJsonView;

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
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("requestedContextValidator")
    private ObjectProvider<RequestedContextValidator> requestedContextValidator;

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
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("serviceValidationAuthorizers")
    private ObjectProvider<ServiceTicketValidationAuthorizersExecutionPlan> validationAuthorizers;

    @Bean
    public AbstractDelegateController smartOpenIdAssociationController() {
        return new SmartOpenIdController(serverManager(), casOpenIdAssociationSuccessView.getIfAvailable());
    }

    @RefreshScope
    @Bean
    public ServerManager serverManager() {
        val manager = new ServerManager();
        manager.setOPEndpointUrl(casProperties.getServer().getLoginUrl());
        manager.setEnforceRpId(casProperties.getAuthn().getOpenid().isEnforceRpId());
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        LOGGER.info("Creating openid server manager with OP endpoint [{}]", casProperties.getServer().getLoginUrl());
        return manager;
    }

    @ConditionalOnMissingBean(name = "openIdServiceResponseBuilder")
    @Bean
    public ResponseBuilder openIdServiceResponseBuilder() {
        val openIdPrefixUrl = casProperties.getServer().getPrefix().concat("/openid");
        return new OpenIdServiceResponseBuilder(openIdPrefixUrl, serverManager(), centralAuthenticationService.getIfAvailable(), servicesManager.getIfAvailable());
    }


    @Bean
    @RefreshScope
    public YadisController yadisController() {
        return new YadisController();
    }


    @Bean
    @RefreshScope
    public OpenIdProviderController openIdProviderController() {
        return new OpenIdProviderController();
    }

    @Bean
    public Action openIdSingleSignOnAction() {
        return new OpenIdSingleSignOnAction(initialAuthenticationAttemptWebflowEventResolver.getIfAvailable(),
            serviceTicketRequestWebflowEventResolver.getIfAvailable(),
            adaptiveAuthenticationPolicy.getIfAvailable(),
            defaultOpenIdUserNameExtractor(),
            ticketRegistrySupport.getIfAvailable());
    }

    @Bean
    public OpenIdUserNameExtractor defaultOpenIdUserNameExtractor() {
        return new DefaultOpenIdUserNameExtractor();
    }

    @Bean
    public OpenIdPostUrlHandlerMapping openIdPostUrlHandlerMapping() {
        val c = new OpenIdValidateController(cas20WithoutProxyProtocolValidationSpecification.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            servicesManager.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            proxy20Handler.getIfAvailable(),
            argumentExtractor.getIfAvailable(),
            requestedContextValidator.getIfAvailable(),
            cas3ServiceJsonView.getIfAvailable(),
            casOpenIdServiceSuccessView.getIfAvailable(),
            casOpenIdServiceFailureView.getIfAvailable(),
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            serverManager(),
            validationAuthorizers.getIfAvailable(),
            casProperties.getSso().isRenewAuthnEnabled());

        val controller = new DelegatingController();
        controller.setDelegates(CollectionUtils.wrapList(smartOpenIdAssociationController(), c));

        val m = new OpenIdPostUrlHandlerMapping();
        m.setOrder(1);
        val mappings = new Properties();
        mappings.put("/login", controller);
        m.setMappings(mappings);
        return m;
    }
}
