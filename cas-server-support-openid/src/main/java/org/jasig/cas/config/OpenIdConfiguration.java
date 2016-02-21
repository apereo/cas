package org.jasig.cas.config;

import org.jasig.cas.web.AbstractDelegateController;
import org.jasig.cas.web.DelegatingController;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

import java.util.Arrays;

/**
 * This this {@link OpenIdConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("openidConfiguration")
public class OpenIdConfiguration {
    
    @Autowired
    @Qualifier("smartOpenIdAssociationController")
    private AbstractDelegateController smartOpenIdAssociationController;
        
    @Autowired
    @Qualifier("openIdValidateController")
    private AbstractDelegateController openIdValidateController;
    
    @Value("${server.prefix}/login")
    private String endpoint;
    
    @Value("${cas.openid.enforce.rpid:false}")
    private boolean enforceRpId;

    @Bean(name="casOpenIdServiceFailureView")
    public JstlView casOpenIdServiceFailureView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdServiceFailureView.jsp");
    }
    
    @Bean(name="casOpenIdServiceSuccessView")
    public JstlView casOpenIdServiceSuccessView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdServiceSuccessView.jsp");
    }
    
    @Bean(name="casOpenIdAssociationFailureView")
    public JstlView casOpenIdAssociationFailureView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdAssociationFailureView.jsp");
    }
    
    @Bean(name="casOpenIdAssociationSuccessView")
    public JstlView casOpenIdAssociationSuccessView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/casOpenIdAssociationSuccessView.jsp");
    }
    
    @Bean(name="openIdProviderView")
    public JstlView openIdProviderView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/user.jsp");
    }
    
    @Bean(name="openidDelegatingController")
    public DelegatingController openidDelegatingController() {
        final DelegatingController controller = new DelegatingController();
        controller.setDelegates(Arrays.asList(this.smartOpenIdAssociationController, 
                this.openIdValidateController));
        return controller;
    }

    @Bean(name="serverManager")
    public ServerManager serverManager() {
        final ServerManager manager = new ServerManager();
        manager.setOPEndpointUrl(this.endpoint);
        manager.setEnforceRpId(this.enforceRpId);
        return manager;
    }
}
