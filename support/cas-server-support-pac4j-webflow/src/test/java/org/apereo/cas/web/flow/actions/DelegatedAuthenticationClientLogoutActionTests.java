package org.apereo.cas.web.flow.actions;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.flow.actions.logout.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationClientLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedAuthenticationClientLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
    private Action delegatedAuthenticationClientLogoutAction;

    @Autowired
    @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
    private LogoutManager logoutManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Test
    void verifyOperationWithProfile() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.setClientName("CasClient");
        context.getHttpServletRequest().setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, context.getHttpServletResponse().getStatus());
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(context, DelegatedAuthenticationClientLogoutRequest.class));
        val tgt = new MockTicketGrantingTicket("casuser");

        logoutManager.performLogout(SingleLogoutExecutionRequest.builder()
            .httpServletRequest(Optional.of(context.getHttpServletRequest()))
            .httpServletResponse(Optional.of(context.getHttpServletResponse()))
            .ticketGrantingTicket(tgt)
            .build());
        assertNull(context.getHttpServletRequest().getSession(false));
    }

    @Test
    void verifyOperationWithNoProfile() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertNotEquals(HttpStatus.SC_MOVED_TEMPORARILY, context.getHttpServletResponse().getStatus());
    }
}
