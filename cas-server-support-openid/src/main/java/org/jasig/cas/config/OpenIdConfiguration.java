package org.jasig.cas.config;

import java.util.Arrays;

import org.jasig.cas.support.openid.web.mvc.OpenIdValidateController;
import org.jasig.cas.support.openid.web.mvc.SmartOpenIdController;
import org.jasig.cas.web.AbstractDelegateController;
import org.jasig.cas.web.DelegatingController;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This this {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("openidConfiguration")
public class OpenIdConfiguration {

    /**
     * The Endpoint.
     */
    @Value("${server.prefix}/login")
    private String endpoint;

    /**
     * The Enforce rp id.
     */
    @Value("${cas.openid.enforce.rpid:false}")
    private boolean enforceRpId;

    /**
     * Cas open id service failure view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casOpenIdServiceFailureView")
    public JstlView casOpenIdServiceFailureView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdServiceFailureView.jsp");
    }

    /**
     * Cas open id service success view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casOpenIdServiceSuccessView")
    public JstlView casOpenIdServiceSuccessView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdServiceSuccessView.jsp");
    }

    /**
     * Cas open id association failure view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casOpenIdAssociationFailureView")
    public JstlView casOpenIdAssociationFailureView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdAssociationFailureView.jsp");
    }

    /**
     * Cas open id association success view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="casOpenIdAssociationSuccessView")
    public JstlView casOpenIdAssociationSuccessView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdAssociationSuccessView.jsp");
    }

    /**
     * Open id provider view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name="openIdProviderView")
    public JstlView openIdProviderView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/user.jsp");
    }

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
    @Bean(name="serverManager")
    public ServerManager serverManager() {
        final ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(this.endpoint);
        manager.setEnforceRpId(this.enforceRpId);
        return manager;
    }
}
