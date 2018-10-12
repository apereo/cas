package org.apereo.cas.config;

import org.apereo.cas.TestOneTimePasswordAuthenticationHandler;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;

import lombok.val;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.HashMap;

/**
 * This is {@link CasMultifactorTestAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@TestConfiguration("CasMultifactorTestAuthenticationEventExecutionPlanConfiguration")
public class CasMultifactorTestAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        val users = new HashMap<String, String>();
        users.put("alice", "alice");
        users.put("bob", "bob");
        users.put("mallory", "mallory");

        val credentials = new HashMap<String, String>();
        credentials.put("alice", "31415");
        credentials.put("bob", "62831");
        credentials.put("mallory", "14142");

        plan.registerAuthenticationHandler(new AcceptUsersAuthenticationHandler("", null, null, null, users));
        plan.registerAuthenticationHandler(new TestOneTimePasswordAuthenticationHandler(credentials));
    }
}
