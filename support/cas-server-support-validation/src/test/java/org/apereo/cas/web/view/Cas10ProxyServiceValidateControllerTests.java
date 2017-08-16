package org.apereo.cas.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.*;

/**
 * This is {@link Cas10ProxyServiceValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class Cas10ProxyServiceValidateControllerTests extends Cas20ResponseViewTests {
    @Override
    protected ProxyHandler getProxyHandler() throws Exception {
        return new Cas10ProxyHandler();
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncoding() throws Exception {
        final Service svc = CoreAuthenticationTestUtils.getService("http://www.jasig.org?param=hello+world");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://www.jasig.org?param=hello%20world";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, CoreAuthenticationTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertTrue(this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        final ModelAndView modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
                .getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        final String origSvc = "http://www.jasig.org?param=hello+world";
        final Service svc = CoreAuthenticationTestUtils.getService(origSvc);
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, CoreAuthenticationTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "duh");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }
}
