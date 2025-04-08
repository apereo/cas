package org.apereo.cas.web.flow.actions;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
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
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.setClientName("CasClient");
        context.setRequestAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, context.getHttpServletResponse().getStatus());
        val delegatedAuthenticationLogoutRequest = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(context, DelegatedAuthenticationClientLogoutRequest.class);
        assertNotNull(delegatedAuthenticationLogoutRequest);
        assertNotNull(delegatedAuthenticationLogoutRequest.getTarget());
        assertNotNull(delegatedAuthenticationLogoutRequest.getLocation());
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
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertNotEquals(HttpStatus.SC_MOVED_TEMPORARILY, context.getHttpServletResponse().getStatus());
    }
}
