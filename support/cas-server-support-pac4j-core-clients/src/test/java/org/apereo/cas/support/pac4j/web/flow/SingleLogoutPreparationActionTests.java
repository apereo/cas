package org.apereo.cas.support.pac4j.web.flow;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.service.ProfileService;
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
 */
public class SingleLogoutPreparationActionTests {

    private static final String TGT_ID = "TGT-1";

    private SingleLogoutPreparationAction actionUnderTest;

    private CommonProfile profile;


    /**
     * Tests that the action properly populates the session and the request with a PAC4J profile.
     * 
     * The profile is first retrieved from the long-term profile service, the key used for the store is the TGT ID.
     * The TGT is retrieved from the cookie generator - the current request should hold an encrypted TGT cookie.
     */
    @Test
    public void actionShouldPopulateRequestAndSessionWithPac4JProfile() throws Exception {
        // Prepare the input
        MockHttpServletRequest nativeRequest = new MockHttpServletRequest();
        MockHttpServletResponse nativeResponse = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        nativeRequest.setSession(session);
        MockServletContext servletContext = new MockServletContext();
        ServletExternalContext externalContext = new ServletExternalContext(servletContext, nativeRequest, nativeResponse);
        MockRequestContext rc = new MockRequestContext();
        rc.setExternalContext(externalContext);

        // Run the tested action
        Event e = actionUnderTest.doExecute(rc);

        // Check the result
        assertEquals("success", e.getId());

        // Check request and session attributes
        Assert.assertNotNull("No user profile was saved into the request.", nativeRequest.getAttribute(Pac4jConstants.USER_PROFILES));
        Assert.assertNotNull("No user profile was saved into the session.", session.getAttribute(Pac4jConstants.USER_PROFILES));
    }


    @Before
    public void setUpTestedObject() {
        profile = new CommonProfile();
        profile.setClientName("UnitTestClient");
        profile.setId("Profile-1");

        ProfileService<CommonProfile> profileServiceMock = mock(ProfileService.class);
        when(profileServiceMock.findById(TGT_ID)).thenReturn(profile);

        CookieRetrievingCookieGenerator ticketGrantingTicketCookieGeneratorMock = mock(CookieRetrievingCookieGenerator.class);
        when(ticketGrantingTicketCookieGeneratorMock.retrieveCookieValue(any(HttpServletRequest.class))).thenReturn(TGT_ID);

        actionUnderTest = new SingleLogoutPreparationAction(ticketGrantingTicketCookieGeneratorMock, profileServiceMock);
    }

}
