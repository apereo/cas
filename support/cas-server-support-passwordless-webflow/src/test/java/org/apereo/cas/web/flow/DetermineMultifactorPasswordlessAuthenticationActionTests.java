package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DetermineMultifactorPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowActions")
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
    "cas.authn.passwordless.multifactor-authentication-activated=true",
    "cas.authn.mfa.global-provider-id=" + TestMultifactorAuthenticationProvider.ID
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DetermineMultifactorPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("determineMultifactorPasswordlessAuthenticationAction")
    private Action determineMultifactorPasswordlessAuthenticationAction;

    @Test
    @Order(1)
    public void verifyUserMfaActionDisabled() throws Exception {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);
        val request = new MockHttpServletRequest();
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .multifactorAuthenticationEligible(TriStateBoolean.FALSE)
            .build();
        WebUtils.putPasswordlessAuthenticationAccount(context, account);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    @Order(2)
    public void verifyUserMfaActionNoProvider() throws Exception {
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);
        val request = new MockHttpServletRequest();
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .multifactorAuthenticationEligible(TriStateBoolean.TRUE)
            .build();
        WebUtils.putPasswordlessAuthenticationAccount(context, account);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    @Order(3)
    public void verifyUserMissing() throws Exception {
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    @Order(100)
    public void verifyAction() throws Exception {
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);
        val request = new MockHttpServletRequest();
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .build();
        WebUtils.putPasswordlessAuthenticationAccount(context, account);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(TestMultifactorAuthenticationProvider.ID, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
    }
}
