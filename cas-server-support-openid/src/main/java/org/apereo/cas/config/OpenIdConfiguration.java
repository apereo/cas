package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.ServiceFactory;
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
import org.apereo.cas.web.AbstractDelegateController;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.DelegatingController;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;

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
    private CasConfigurationProperties casProperties;

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
        return new SmartOpenIdController();
    }

    /**
     * OpenId validate controller.
     * Handles signature verification requests.
     *
     * @return the signature verification controller
     */
    @Bean
    public AbstractDelegateController openIdValidateController() {
        return new OpenIdValidateController();
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
        manager.setEnforceRpId(casProperties.getOpenid().isEnforceRpId());
        LOGGER.info("Creating openid server manager with OP endpoint {}", casProperties.getServer().getLoginUrl());
        return manager;
    }

    @Bean
    public BaseApplicationContextWrapper openIdApplicationContextWrapper() {
        return new OpenIdApplicationContextWrapper();
    }

    @Bean
    public AuthenticationHandler openIdCredentialsAuthenticationHandler() {
        return new OpenIdCredentialsAuthenticationHandler();
    }

    @Bean
    public OpenIdPrincipalResolver openIdPrincipalResolver() {
        return new OpenIdPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public ServiceFactory openIdServiceFactory() {
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
        return new OpenIdPostUrlHandlerMapping();
    }


}
