package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.mock.MockValidationSpecification;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationResponseType;
import org.apereo.cas.web.config.CasValidationConfiguration;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Import({
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {
    protected static final String SUCCESS = "Success";

    protected static final Service SERVICE = RegisteredServiceTestUtils.getService("https://www.casinthecloud.com");

    protected static final Service DEFAULT_SERVICE = RegisteredServiceTestUtils.getService();

    private static final String GITHUB_URL = "https://www.github.com";

    protected AbstractServiceValidateController serviceValidateController;

    protected static CasProtocolValidationSpecification getValidationSpecification() {
        return new Cas20WithoutProxyingValidationSpecification(mock(ServicesManager.class));
    }

    protected static ProxyHandler getProxyHandler() {
        return new Cas20ProxyHandler(new SimpleHttpClientFactoryBean().getObject(), new DefaultUniqueTicketIdGenerator());
    }

    @BeforeEach
    public void onSetUp() {
        val context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = getServiceValidateControllerInstance();
        this.serviceValidateController.setApplicationContext(context);
    }

    protected HttpServletRequest getHttpServletRequest() {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);
        val sId2 = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, null);

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

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler((credential, proxyGrantingTicketId) -> null);
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicket() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        val mv = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(mv.getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketInvalidSpec() throws Exception {
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyRenewSpecFailsCorrectly() throws Exception {
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyInvalidServiceTicket() throws Exception {
        val ctx = CoreAuthenticationTestUtils
            .getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        getCentralAuthenticationService().getObject().destroyTicketGrantingTicket(tId.getId());

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNotNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtButNoProxyHandlingBecausePgtIsReleased() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), DEFAULT_SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), DEFAULT_SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, DEFAULT_SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, DEFAULT_SERVICE.getId());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndPgtUrlMismatch() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "http://www.github.com");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    public void verifyValidServiceTicketAndFormatAsJson() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, ValidationResponseType.JSON.name());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains("Json"));
    }

    @Test
    public void verifyValidServiceTicketAndBadFormat() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, "NOTHING");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains("Success"));
    }

    @Test
    public void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.addValidationSpecification(new MockValidationSpecification(false));
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    /*
        CAS10 Proxying Tests.
     */
    @Test
    public void verifyValidServiceTicketWithDifferentEncoding() throws Exception {
        val svc = RegisteredServiceTestUtils.getService("http://www.jasig.org?param=hello+world");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://www.jasig.org?param=hello%20world";
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        val modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl(SERVICE);
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
            .getView()).toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        val origSvc = "http://www.jasig.org?param=hello+world";
        val svc = RegisteredServiceTestUtils.getService(origSvc);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView())
            .toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "duh");
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    /*
    Helper methods.
     */
    protected ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl(final Service service) throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val tId = getCentralAuthenticationService().getObject().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().getObject().grantServiceTicket(tId.getId(), service, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, GITHUB_URL);

        return this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
    }

    public abstract AbstractServiceValidateController getServiceValidateControllerInstance();
}
