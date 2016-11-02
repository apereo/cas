package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.flow.OAuth20LogoutAction;
import org.apereo.cas.support.oauth.web.flow.OAuth20LogoutWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.ApplicationLogoutController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link CasOAuthWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casOAuthWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuthWebflowConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    private FlowDefinitionRegistry logoutFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name = "oauth20LogoutWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer oauth20LogoutWebflowConfigurer() {
        final OAuth20LogoutWebflowConfigurer c = new OAuth20LogoutWebflowConfigurer();
        c.setFlowBuilderServices(this.flowBuilderServices);
        c.setLoginFlowDefinitionRegistry(this.loginFlowDefinitionRegistry);
        c.setLogoutFlowDefinitionRegistry(this.logoutFlowDefinitionRegistry);
        return c;
    }

    @Autowired
    @Bean
    public OAuth20LogoutAction oauth20LogoutAction(@Qualifier("oauthSecConfig") final Config oauthSecConfig) {
        final ApplicationLogoutController controller = new ApplicationLogoutController();
        controller.setConfig(oauthSecConfig);
        final OAuth20LogoutAction action = new OAuth20LogoutAction();
        action.setApplicationLogoutController(controller);
        return action;
    }
}
