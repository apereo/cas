package org.apereo.cas.logout.config;

import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutManagerImpl;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreLogoutConfiguration")
public class CasCoreLogoutConfiguration {
    
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder defaultSingleLogoutServiceLogoutUrlBuilder() {
        return new DefaultSingleLogoutServiceLogoutUrlBuilder();
    }
    
    @Bean
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        return new DefaultSingleLogoutServiceMessageHandler();
    }
    
    @RefreshScope
    @Bean
    public LogoutManager logoutManager() {
        return new LogoutManagerImpl();
    }
    
    @Bean
    public LogoutMessageCreator logoutBuilder() {
        return new SamlCompliantLogoutMessageCreator();
    }
}
