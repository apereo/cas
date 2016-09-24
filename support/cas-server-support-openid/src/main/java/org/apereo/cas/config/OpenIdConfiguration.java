package org.apereo.cas.config;

import com.google.common.collect.Lists;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.support.openid.authentication.principal.OpenIdService;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.support.openid.web.OpenIdProviderController;
import org.apereo.cas.support.openid.web.flow.OpenIdSingleSignOnAction;
import org.apereo.cas.support.openid.web.mvc.OpenIdValidateController;
import org.apereo.cas.support.openid.web.mvc.SmartOpenIdController;
import org.apereo.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.apereo.cas.support.openid.web.support.OpenIdPostUrlHandlerMapping;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.ValidationSpecification;
import org.apereo.cas.web.AbstractDelegateController;
import org.apereo.cas.web.DelegatingController;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("openidConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OpenIdConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConfiguration.class);

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    
    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("casOpenIdServiceSuccessView")
    private View casOpenIdServiceSuccessView;

    @Autowired
    @Qualifier("casOpenIdServiceFailureView")
    private View casOpenIdServiceFailureView;

    @Autowired
    @Qualifier("casOpenIdAssociationSuccessView")
    private View casOpenIdAssociationSuccessView;

    @Autowired
    @Qualifier("proxy20Handler")
    private ProxyHandler proxy20Handler;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
    private ValidationSpecification cas20WithoutProxyProtocolValidationSpecification;

    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("uniqueIdGeneratorsMap")
    private Map uniqueIdGeneratorsMap;


    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;
    
    @Bean
    public DelegatingController openidDelegatingController() {
        final DelegatingController controller = new DelegatingController();
        controller.setDelegates(Lists.newArrayList(smartOpenIdAssociationController(), openIdValidateController()));
        return controller;
    }

    @Bean
    public AbstractDelegateController smartOpenIdAssociationController() {
        final SmartOpenIdController b = new SmartOpenIdController();
        b.setServerManager(serverManager());
        b.setSuccessView(this.casOpenIdAssociationSuccessView);
        return b;
    }

    @Bean
    public AbstractDelegateController openIdValidateController() {
        final OpenIdValidateController c = new OpenIdValidateController();
        c.setServerManager(serverManager());
        c.setValidationSpecification(this.cas20WithoutProxyProtocolValidationSpecification);
        c.setSuccessView(casOpenIdServiceSuccessView);
        c.setFailureView(casOpenIdServiceFailureView);
        c.setProxyHandler(proxy20Handler);
        c.setAuthenticationSystemSupport(authenticationSystemSupport);
        c.setServicesManager(servicesManager);
        c.setCentralAuthenticationService(centralAuthenticationService);
        c.setArgumentExtractor(argumentExtractor);
        c.setMultifactorTriggerSelectionStrategy(multifactorTriggerSelectionStrategy);
        c.setAuthenticationContextValidator(authenticationContextValidator);
        c.setJsonView(cas3ServiceJsonView);
        c.setAuthnContextAttribute(casProperties.getAuthn().getMfa().getAuthenticationContextAttribute());

        return c;
    }

    @RefreshScope
    @Bean
    public ServerManager serverManager() {
        final ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(casProperties.getServer().getLoginUrl());
        manager.setEnforceRpId(casProperties.getAuthn().getOpenid().isEnforceRpId());
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        LOGGER.info("Creating openid server manager with OP endpoint {}", casProperties.getServer().getLoginUrl());
        return manager;
    }


    @Bean
    public AuthenticationHandler openIdCredentialsAuthenticationHandler() {
        final OpenIdCredentialsAuthenticationHandler h = new OpenIdCredentialsAuthenticationHandler();
        h.setTicketRegistry(this.ticketRegistry);
        h.setPrincipalFactory(openidPrincipalFactory());
        h.setServicesManager(servicesManager);
        return h;
    }

    @Bean
    public OpenIdPrincipalResolver openIdPrincipalResolver() {
        final OpenIdPrincipalResolver r = new OpenIdPrincipalResolver();
        r.setAttributeRepository(attributeRepository);
        r.setPrincipalAttributeName(casProperties.getAuthn().getOpenid().getPrincipal().getPrincipalAttribute());
        r.setReturnNullIfNoAttributes(casProperties.getAuthn().getOpenid().getPrincipal().isReturnNull());
        r.setPrincipalFactory(openidPrincipalFactory());
        return r;
    }

    @Bean
    public PrincipalFactory openidPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public OpenIdServiceFactory openIdServiceFactory() {
        final OpenIdServiceFactory f = new OpenIdServiceFactory();
        f.setOpenIdPrefixUrl(casProperties.getServer().getPrefix().concat("/openid"));
        return f;
    }

    @Bean
    @RefreshScope
    public OpenIdProviderController openIdProviderController() {
        return new OpenIdProviderController();
    }


    @Bean
    public Action openIdSingleSignOnAction() {
        final OpenIdSingleSignOnAction a = new OpenIdSingleSignOnAction();
        a.setExtractor(defaultOpenIdUserNameExtractor());
        a.setTicketRegistrySupport(ticketRegistrySupport);
        a.setAdaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy);
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
        return a;
    }

    @Bean
    public OpenIdUserNameExtractor defaultOpenIdUserNameExtractor() {
        return new DefaultOpenIdUserNameExtractor();
    }

    @Bean
    public OpenIdPostUrlHandlerMapping openIdPostUrlHandlerMapping() {
        final OpenIdPostUrlHandlerMapping m = new OpenIdPostUrlHandlerMapping();
        m.setOrder(1);
        final Properties mappings = new Properties();
        mappings.put("/login", openidDelegatingController());
        m.setMappings(mappings);
        return m;
    }


    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(openIdCredentialsAuthenticationHandler(), openIdPrincipalResolver());
        uniqueIdGeneratorsMap.put(OpenIdService.class.getCanonicalName(), this.serviceTicketUniqueIdGenerator);
        this.argumentExtractor.getServiceFactories().add(0, openIdServiceFactory());
    }
}
