package org.apereo.cas.adaptors.fortress.config;

import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.rest.AccessMgrRestImpl;
import org.apereo.cas.adaptors.fortress.FortressAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;

@Configuration("fortressAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class FortressAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "fortressPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory fortressPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    public AccessMgr fortressAccessManager() {
        AccessMgrRestImpl accessMgrRestImpl = new AccessMgrRestImpl();
        accessMgrRestImpl.setContextId(casProperties.getFortressProperties().getRbacContextId());
        return accessMgrRestImpl;
    }

    @ConditionalOnMissingBean(name = "fortressAuthenticationHandler")
    @Bean
    @RefreshScope
    public Collection<AuthenticationHandler> fortressAuthenticationHandler() {
        final Collection<AuthenticationHandler> handlers = new HashSet<>();
        handlers.add(new FortressAuthenticationHandler(casProperties.getFortressProperties().getName(), servicesManager,
                fortressPrincipalFactory(), null));
        return handlers;
    }

    @Configuration("fortressAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class FortressAuthenticationEventExecutionPlanConfiguration
            implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            fortressAuthenticationHandler().forEach(handler -> {
                plan.registerAuthenticationHandler(handler);
            });
        }
    }

}
