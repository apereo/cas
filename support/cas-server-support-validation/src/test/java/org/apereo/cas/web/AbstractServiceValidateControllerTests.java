package org.apereo.cas.web;

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

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Import({CasProtocolViewsConfiguration.class, CasValidationConfiguration.class, ThymeleafAutoConfiguration.class})
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {
    protected static final String SUCCESS = "Success";
    protected static final Service SERVICE = RegisteredServiceTestUtils.getService();

    private static final String GITHUB_URL = "https://www.github.com";

    protected AbstractServiceValidateController serviceValidateController;

    @BeforeEach
    public void onSetUp() throws Exception {
        val context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = getServiceValidateControllerInstance();
        this.serviceValidateController.setApplicationContext(context);
    }

    protected HttpServletRequest getHttpServletRequest() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);
        val sId2 = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, null);

        val request = new MockHttpServletRequest();
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
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.setProxyHandler((credential, proxyGrantingTicketId) -> null);
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicket() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        val mv = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
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
        val ctx = CoreAuthenticationTestUtils
            .getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId.getId());

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertFalse(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }


    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNotNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndPgtUrlMismatch() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "http://www.github.com");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndFormatAsJson() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, ValidationResponseType.JSON.name());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains("Json"));
    }

    @Test
    public void verifyValidServiceTicketAndBadFormat() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, "NOTHING");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
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
        val svc = RegisteredServiceTestUtils.getService("http://www.jasig.org?param=hello+world");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://www.jasig.org?param=hello%20world";
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        val modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
            .getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        val origSvc = "http://www.jasig.org?param=hello+world";
        val svc = RegisteredServiceTestUtils.getService(origSvc);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "duh");
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    /*
    Helper methods.
     */
    protected ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
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
