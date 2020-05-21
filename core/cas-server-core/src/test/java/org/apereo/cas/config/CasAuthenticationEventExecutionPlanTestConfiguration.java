package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasAuthenticationEventExecutionPlanTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestConfiguration("casTestAuthenticationEventExecutionPlanConfiguration")
@Lazy(false)
public class CasAuthenticationEventExecutionPlanTestConfiguration {
    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private PrincipalResolver defaultPrincipalResolver;

    @Bean
    public AuthenticationEventExecutionPlanConfigurer casDefaultAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver);
    }

}
