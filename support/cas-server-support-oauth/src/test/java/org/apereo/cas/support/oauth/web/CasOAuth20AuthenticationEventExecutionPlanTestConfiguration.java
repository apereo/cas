package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * This is {@link CasOAuth20AuthenticationEventExecutionPlanTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("OAuth")
@TestConfiguration(value = "CasOAuth20AuthenticationEventExecutionPlanTestConfiguration", proxyBeanMethods = false)
public class CasOAuth20AuthenticationEventExecutionPlanTestConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
    private PrincipalResolver defaultPrincipalResolver;

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandlerWithPrincipalResolver(new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver);
    }
}
