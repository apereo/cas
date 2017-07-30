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

/**
 * This is {@link FortressAuthenticationConfiguration}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */
@Configuration("fortressAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class FortressAuthenticationConfiguration {

    private static final String FORTRESS_HANDLER_NAME = "fortressHandler";
    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationConfiguration.class);

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

    @Bean
    public AccessMgr fortressAccessManager() {
        final String rbacContext = casProperties.getAuthn().getFortress().getRbaccontext();
        LOGGER.trace("registering fortress access manager with context : {}", rbacContext);
        final AccessMgrRestImpl accessMgrRestImpl = new AccessMgrRestImpl();
        accessMgrRestImpl.setContextId(casProperties.getAuthn().getFortress().getRbaccontext());
        return accessMgrRestImpl;
    }

    @ConditionalOnMissingBean(name = "fortressAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler fortressAuthenticationHandler() {
        LOGGER.info("registering fortress authentication handler");
        return new FortressAuthenticationHandler(FORTRESS_HANDLER_NAME, servicesManager,
                fortressPrincipalFactory(), null);
    }

    /**
     * Fortress Authentication event execution plan configuration.
     */
    @Configuration("fortressAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class FortressAuthenticationEventExecutionPlanConfiguration
            implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            LOGGER.info("registering fortress authentication event execution plan");
            plan.registerAuthenticationHandler(fortressAuthenticationHandler());
        }
    }

}
