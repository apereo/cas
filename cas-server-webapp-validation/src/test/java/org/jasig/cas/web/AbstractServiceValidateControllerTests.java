package org.jasig.cas.web;

import org.jasig.cas.AbstractCentralAuthenticationServiceTests;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.mock.MockValidationSpecification;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.util.http.SimpleHttpClientFactoryBean;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.validation.ValidationResponseType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {

    private static final Service SERVICE = org.jasig.cas.authentication.TestUtils.getService();
    protected AbstractServiceValidateController serviceValidateController;


    @Before
    public void onSetUp() throws Exception {
        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController.setCentralAuthenticationService(getCentralAuthenticationService());
        this.serviceValidateController.setAuthenticationSystemSupport(getAuthenticationSystemSupport());
        final Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler();
        proxyHandler.setHttpClient(new SimpleHttpClientFactoryBean().getObject());
        this.serviceValidateController.setProxyHandler(proxyHandler);
        this.serviceValidateController.setApplicationContext(context);
        this.serviceValidateController.setArgumentExtractor(getArgumentExtractor());
        this.serviceValidateController.setServicesManager(getServicesManager());
    }

    private HttpServletRequest getHttpServletRequest() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);
        final ServiceTicket sId2 = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, null);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId2.getId());
        request.addParameter("renew", "true");

        return request;
    }

    @Test
    public void verifyAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
    }

    @Test
    public void verifyEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
                new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    public void verifyValidServiceTicket() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(),
                SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());

        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void verifyValidServiceTicketInvalidSpec() throws Exception {
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
    }

    @Test(expected=RuntimeException.class)
    public void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(MockValidationSpecification.class);

        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
        fail("Expected exception");
    }

    @Test
    public void verifyInvalidServiceTicket() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(),
                SERVICE, ctx);

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId.getId());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());

        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final ModelAndView modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(),
                SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "duh");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(),
                SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNotNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(),
                SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");

        this.serviceValidateController.setProxyHandler(new ProxyHandler() {
            @Override
            public String handle(final Credential credential, final TicketGrantingTicket proxyGrantingTicketId) {
                return null;
            }
            
            @Override
            public boolean canHandle(final Credential credential) {
                return true;
            }
        });
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        final String origSvc = "http://www.jasig.org?param=hello+world";
        final Service svc = org.jasig.cas.authentication.TestUtils.getService(origSvc);
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), svc);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        

        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", org.jasig.cas.authentication.TestUtils.getService(reqSvc).getId());
        request.addParameter("ticket", sId.getId());
        
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }
    
    @Test
    public void verifyValidServiceTicketWithDifferentEncoding() throws Exception {
        final Service svc = org.jasig.cas.authentication.TestUtils.getService("http://www.jasig.org?param=hello+world");
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), svc);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService()
                .grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://www.jasig.org?param=hello%20world";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", org.jasig.cas.authentication.TestUtils.getService(reqSvc).getId());
        request.addParameter("ticket", sId.getId());
        
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }
    
    @Test
    public void verifyValidServiceTicketAndPgtUrlMismatch() throws Exception {
        final Service svc = org.jasig.cas.authentication.TestUtils.getService("proxyService");
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", svc.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "http://www.github.com");
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }

    @Test
    public void verifyValidServiceTicketAndFormatAsJson() throws Exception {
        final Service svc = org.jasig.cas.authentication.TestUtils.getService("proxyService");
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", svc.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("format", ValidationResponseType.JSON.name());

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(modelAndView.getViewName(), AbstractServiceValidateController.DEFAULT_SERVICE_VIEW_NAME_JSON);
    }

    @Test
    public void verifyValidServiceTicketAndBadFormat() throws Exception {
        final Service svc = org.jasig.cas.authentication.TestUtils.getService("proxyService");
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);


        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", svc.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("format", "NOTHING");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(modelAndView.getViewName(), AbstractServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME);
    }

    protected final ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl() throws Exception {
        final AuthenticationContext ctx = org.jasig.cas.authentication.TestUtils
                .getAuthenticationContext(getAuthenticationSystemSupport(), SERVICE);
        final TicketGrantingTicket tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", SERVICE.getId());
        request.addParameter("ticket", sId.getId());
        request.addParameter("pgtUrl", "https://www.github.com");


        return this.serviceValidateController
                .handleRequestInternal(request, new MockHttpServletResponse());
    }

}
