package org.apereo.cas.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockValidationSpecification;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationResponseType;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.junit.Test;
import org.junit.Before;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Import;
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
@Import({CasProtocolViewsConfiguration.class, CasValidationConfiguration.class, ThymeleafAutoConfiguration.class})
@Slf4j
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {
    protected static final String SUCCESS = "Success";
    protected static final Service SERVICE = RegisteredServiceTestUtils.getService();

    private static final String GITHUB_URL = "https://www.github.com";

    protected AbstractServiceValidateController serviceValidateController;

    @Before
    public void onSetUp() throws Exception {
        final var context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = getServiceValidateControllerInstance();
        this.serviceValidateController.setApplicationContext(context);
    }

    protected HttpServletRequest getHttpServletRequest() {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);
        final var sId2 = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, null);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId2.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        return request;
    }

    @Test
    public void verifyEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
                new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.setProxyHandler((credential, proxyGrantingTicketId) -> null);
        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicket() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        final var mv = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(mv.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketInvalidSpec() throws Exception {
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }


    @Test
    public void verifyRenewSpecFailsCorrectly() throws Exception {
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyInvalidServiceTicket() throws Exception {
        final var ctx = CoreAuthenticationTestUtils
                .getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId.getId());

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertFalse(this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }


    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNotNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndPgtUrlMismatch() throws Exception {
        final Service svc = RegisteredServiceTestUtils.getService("proxyService");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "http://www.github.com");

        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndFormatAsJson() throws Exception {
        final Service svc = RegisteredServiceTestUtils.getService("proxyService");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, ValidationResponseType.JSON.name());

        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains("Json"));
    }

    @Test
    public void verifyValidServiceTicketAndBadFormat() throws Exception {
        final Service svc = RegisteredServiceTestUtils.getService("proxyService");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, "NOTHING");

        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains("Success"));
    }


    @Test
    public void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.addValidationSpecification(new MockValidationSpecification(false));
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }
    
    /*
        CAS10 Proxying Tests.
     */
    @Test
    public void verifyValidServiceTicketWithDifferentEncoding() throws Exception {
        final Service svc = RegisteredServiceTestUtils.getService("http://www.jasig.org?param=hello+world");
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final var reqSvc = "http://www.jasig.org?param=hello%20world";
        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final var modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
                .getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        final var origSvc = "http://www.jasig.org?param=hello+world";
        final Service svc = RegisteredServiceTestUtils.getService(origSvc);
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final var reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "duh");
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final var modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    /*
    Helper methods.
     */
    protected ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl() throws Exception {
        final var ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);
        final var tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final var sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final var request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, GITHUB_URL);

        return this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
    }

    public abstract AbstractServiceValidateController getServiceValidateControllerInstance();

    protected CasProtocolValidationSpecification getValidationSpecification() {
        return new Cas20WithoutProxyingValidationSpecification();
    }

    protected ProxyHandler getProxyHandler() {
        return new Cas20ProxyHandler(new SimpleHttpClientFactoryBean().getObject(), new DefaultUniqueTicketIdGenerator());
    }
}
