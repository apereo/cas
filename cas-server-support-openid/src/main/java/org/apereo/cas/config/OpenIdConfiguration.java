package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.openid.OpenIdApplicationContextWrapper;
import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.apereo.cas.support.openid.authentication.principal.OpenIdPrincipalResolver;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.support.openid.web.OpenIdProviderController;
import org.apereo.cas.support.openid.web.flow.OpenIdSingleSignOnAction;
import org.apereo.cas.support.openid.web.mvc.OpenIdValidateController;
import org.apereo.cas.support.openid.web.mvc.SmartOpenIdController;
import org.apereo.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.apereo.cas.support.openid.web.support.OpenIdPostUrlHandlerMapping;
import org.apereo.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.AbstractDelegateController;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.DelegatingController;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;
import java.util.Properties;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("openidConfiguration")
public class OpenIdConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConfiguration.class);

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

    /**
     * Openid delegating controller delegating controller.
     *
     * @return the delegating controller
     */
    @Bean
    public DelegatingController openidDelegatingController() {
        final DelegatingController controller = new DelegatingController();
        controller.setDelegates(Arrays.asList(
                this.smartOpenIdAssociationController(),
                this.openIdValidateController()));
        return controller;
    }

    /**
     * Smart OpenId Association controller.
     * Handles OpenId association requests.
     *
     * @return the association controller
     */
    @Bean
    public AbstractDelegateController smartOpenIdAssociationController() {
        final SmartOpenIdController b = new SmartOpenIdController();
        b.setServerManager(serverManager());
        return b;
    }

    /**
     * OpenId validate controller.
     * Handles signature verification requests.
     *
     * @return the signature verification controller
     */
    @Bean
    public AbstractDelegateController openIdValidateController() {
        final OpenIdValidateController c = new OpenIdValidateController();
        c.setServerManager(serverManager());
        return c;
    }

    /**
     * Server manager server manager.
     *
     * @return the server manager
     */
    @RefreshScope
    @Bean
    public ServerManager serverManager() {
        final ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(casProperties.getServer().getLoginUrl());
        manager.setEnforceRpId(casProperties.getAuthn().getOpenid().isEnforceRpId());
        LOGGER.info("Creating openid server manager with OP endpoint {}", casProperties.getServer().getLoginUrl());
        return manager;
    }

    @Bean
    public BaseApplicationContextWrapper openIdApplicationContextWrapper() {
        final OpenIdApplicationContextWrapper w = new OpenIdApplicationContextWrapper();
        w.setOpenIdCredentialsAuthenticationHandler(openIdCredentialsAuthenticationHandler());
        w.setOpenIdPrincipalResolver(openIdPrincipalResolver());
        w.setOpenIdServiceFactory(openIdServiceFactory());
        w.setServiceTicketUniqueIdGenerator(this.serviceTicketUniqueIdGenerator);
        return w;
    }

    @Bean
    public AuthenticationHandler openIdCredentialsAuthenticationHandler() {
        final OpenIdCredentialsAuthenticationHandler h = new OpenIdCredentialsAuthenticationHandler();
        h.setTicketRegistry(this.ticketRegistry);
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
        return new OpenIdSingleSignOnAction();
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


}
