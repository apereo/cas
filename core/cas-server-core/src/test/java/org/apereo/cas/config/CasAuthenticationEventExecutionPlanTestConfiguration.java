package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasAuthenticationEventExecutionPlanTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestConfiguration(value = "casTestAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class CasAuthenticationEventExecutionPlanTestConfiguration {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver defaultPrincipalResolver;

    @Bean
    public AuthenticationEventExecutionPlanConfigurer casDefaultAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
            plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver);
        };
    }

}
