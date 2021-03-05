package org.apereo.cas.support.oauth.web;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasOAuth20TestAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("OAuth")
@TestConfiguration("casOAuth20TestAuthenticationEventExecutionPlanConfiguration")
@Lazy(false)
public class CasOAuth20TestAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private PrincipalResolver defaultPrincipalResolver;

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandlerWithPrincipalResolver(new SimpleTestUsernamePasswordAuthenticationHandler(), defaultPrincipalResolver);
    }
}
