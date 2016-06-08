package org.apereo.cas.logout.config;

import org.apereo.cas.configuration.model.core.slo.SloProperties;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutManagerImpl;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * This is {@link CasCoreLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreLogoutConfiguration")
public class CasCoreLogoutConfiguration {

    @Autowired
    private SloProperties properties;

    @Resource(name = "servicesManager")
    private ServicesManager servicesManager;

    @Resource(name = "noRedirectHttpClient")
    private HttpClient httpClient;

    @Bean
    public SingleLogoutServiceLogoutUrlBuilder defaultSingleLogoutServiceLogoutUrlBuilder() {
        return new DefaultSingleLogoutServiceLogoutUrlBuilder();
    }

    @Bean
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        final DefaultSingleLogoutServiceMessageHandler handler =
                new DefaultSingleLogoutServiceMessageHandler();

        handler.setHttpClient(this.httpClient);
        handler.setAsynchronous(properties.isAsynchronous());
        handler.setLogoutMessageBuilder(logoutBuilder());
        handler.setSingleLogoutServiceLogoutUrlBuilder(defaultSingleLogoutServiceLogoutUrlBuilder());

        return handler;
    }

    @RefreshScope
    @Bean
    public LogoutManager logoutManager() {
        final LogoutManagerImpl mgr = new LogoutManagerImpl();
        mgr.setSingleLogoutCallbacksDisabled(properties.isDisabled());
        mgr.setLogoutMessageBuilder(logoutBuilder());
        mgr.setSingleLogoutServiceMessageHandler(defaultSingleLogoutServiceMessageHandler());
        return mgr;
    }

    @Bean
    public LogoutMessageCreator logoutBuilder() {
        return new SamlCompliantLogoutMessageCreator();
    }
}
