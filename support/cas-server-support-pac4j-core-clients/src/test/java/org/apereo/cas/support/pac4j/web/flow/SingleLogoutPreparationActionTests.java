package org.apereo.cas.support.pac4j.web.flow;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.store.Store;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;


/**
 * Unit test of {@link SingleLogoutPreparationAction}.
 * 
 * @author jkacer
 * 
 * @since 5.3.0
 */
public class SingleLogoutPreparationActionTests {

    private static final String TGT_ID = "TGT-1";

    private SingleLogoutPreparationAction actionUnderTest;


    /**
     * Tests that the action properly populates the session and the request with a PAC4J profile.
     * 
     * The profile is first retrieved from the long-term profile service, the key used for the store is the TGT ID.
     * The TGT is retrieved from the cookie generator - the current request should hold an encrypted TGT cookie.
     */
    @Test
    public void actionShouldPopulateRequestAndSessionWithPac4JProfile() throws Exception {
        // Prepare the input
        final MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        final MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        final MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        final MockServletContext servletContext = new MockServletContext();
        final ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        final MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        // Run the tested action
        final Event e = actionUnderTest.doExecute(rc);

        // Check the result
        assertEquals("success", e.getId());

        // Check request and session attributes
        assertNotNull("No user profile was saved into the request.", nativeRequest.getAttribute(Pac4jConstants.USER_PROFILES));
        assertNotNull("No user profile was saved into the session.", session.getAttribute(Pac4jConstants.USER_PROFILES));
    }


    @Before
    public void setUpTestedObject() {
        final CommonProfile profile = new CommonProfile();
        profile.setClientName("UnitTestClient");
        profile.setId("Profile-1");

        final Store<String, CommonProfile> profileStoreMock = mock(Store.class);
        when(profileStoreMock.get(TGT_ID)).thenReturn(profile);

        final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGeneratorMock = mock(CookieRetrievingCookieGenerator.class);
        when(ticketGrantingTicketCookieGeneratorMock.retrieveCookieValue(any(HttpServletRequest.class))).thenReturn(TGT_ID);

        actionUnderTest = new SingleLogoutPreparationAction(ticketGrantingTicketCookieGeneratorMock, profileStoreMock);
    }

}
