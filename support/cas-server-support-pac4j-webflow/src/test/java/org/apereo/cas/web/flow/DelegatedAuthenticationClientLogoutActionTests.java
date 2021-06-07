package org.apereo.cas.web.flow;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

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
@Tag("WebflowAuthenticationActions")
public class DelegatedAuthenticationClientLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT)
    private Action delegatedAuthenticationClientLogoutAction;

    @Autowired
    @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
    private LogoutManager logoutManager;

    @Test
    public void verifyOperationWithProfile() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.setClientName("CasClient");
        request.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
        val tgt = new MockTicketGrantingTicket("casuser");

        logoutManager.performLogout(SingleLogoutExecutionRequest.builder()
            .httpServletRequest(Optional.of(request))
            .httpServletResponse(Optional.of(response))
            .ticketGrantingTicket(tgt)
            .build());
        assertNull(request.getSession(false));
    }

    @Test
    public void verifyOperationWithNoProfile() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertNotEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }
}
