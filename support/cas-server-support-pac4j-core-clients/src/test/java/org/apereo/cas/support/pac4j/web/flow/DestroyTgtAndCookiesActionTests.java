package org.apereo.cas.support.pac4j.web.flow;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;


/**
 * Unit test of {@link DestroyTgtAndCookiesAction}.
 * 
 * Tests only terminate(), which is overridden in the action, not the whole doExecute().
 * 
 * @author jkacer
 */
public class DestroyTgtAndCookiesActionTests {

    private static final String TGT_ID = "TGT-1";

    private DestroyTgtAndCookiesAction actionUnderTest;

    @Mock
    private CentralAuthenticationService casMock;

    @Mock
    private CookieRetrievingCookieGenerator tgtCookieGenMock;

    @Mock
    private CookieRetrievingCookieGenerator warnCookieGenMock;


    @Test
    public void terminateDeletesCookiesAndTgt() {
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
        Event e = actionUnderTest.terminate(rc);
        assertNotNull("Null event returned by the action.", e);
        assertEquals("The result of the action is not success.", "success", e.getId());

        // Did it execute everything needed?
        verify(casMock).destroyTicketGrantingTicket(TGT_ID);
        verify(tgtCookieGenMock).removeCookie(nativeResponse);
        verify(warnCookieGenMock).removeCookie(nativeResponse);
    }

    @Before
    public void setUpTestedAction() {
        MockitoAnnotations.initMocks(this);
        when(casMock.destroyTicketGrantingTicket(TGT_ID)).thenReturn(emptyList());
        when(tgtCookieGenMock.retrieveCookieValue(any(HttpServletRequest.class))).thenReturn(TGT_ID);
        actionUnderTest = new DestroyTgtAndCookiesAction(casMock, tgtCookieGenMock, warnCookieGenMock, new LogoutProperties());
    }

}
