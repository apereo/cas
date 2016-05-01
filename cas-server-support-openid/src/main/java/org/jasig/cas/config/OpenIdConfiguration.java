package org.jasig.cas.config;

import org.jasig.cas.support.openid.web.mvc.OpenIdValidateController;
import org.jasig.cas.support.openid.web.mvc.SmartOpenIdController;
import org.jasig.cas.web.AbstractDelegateController;
import org.jasig.cas.web.DelegatingController;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * This is {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Configuration("openidConfiguration")
public class OpenIdConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdConfiguration.class);
    
    /**
     * The Endpoint.
     */
    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String endpoint;
        
    /**
     * The Enforce rp id.
     */
    @Value("${cas.openid.enforce.rpid:false}")
    private boolean enforceRpId;
    
    /**
     * Openid delegating controller delegating controller.
     *
     * @return the delegating controller
     */
    @Bean(name="openidDelegatingController")
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
    @Bean(name="smartOpenIdAssociationController")
    public AbstractDelegateController smartOpenIdAssociationController() {
        return new SmartOpenIdController();
    }

    /**
     * OpenId validate controller.
     * Handles signature verification requests.
     *
     * @return the signature verification controller
     */
    @Bean(name="openIdValidateController")
    public AbstractDelegateController openIdValidateController() {
        return new OpenIdValidateController();
    }

    /**
     * Server manager server manager.
     *
     * @return the server manager
     */
    @RefreshScope
    @Bean(name="serverManager")
    public ServerManager serverManager() {
        final ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(this.endpoint);
        manager.setEnforceRpId(this.enforceRpId);
        LOGGER.info("Creating openid server manager with OP endpoint {}", this.endpoint);
        return manager;
    }
}
