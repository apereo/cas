package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.mock.MockValidationSpecification;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.DefaultCasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationResponseType;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ExtendWith(CasTestExtension.class)
@ImportAutoConfiguration(CasThemesAutoConfiguration.class)
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {
    protected static final String SUCCESS = "Success";

    protected static final Service SERVICE = RegisteredServiceTestUtils.getService("https://www.casinthecloud.com");

    protected static final Service DEFAULT_SERVICE = RegisteredServiceTestUtils.getService();

    private static final String GITHUB_URL = "https://www.github.com";

    protected AbstractServiceValidateController serviceValidateController;

    protected static CasProtocolValidationSpecification getValidationSpecification() {
        return new DefaultCasProtocolValidationSpecification(mock(ServicesManager.class),
            input -> input.getChainedAuthentications().size() == 1);
    }

    protected static ProxyHandler getProxyHandler() {
        return new Cas20ProxyHandler(new SimpleHttpClientFactoryBean().getObject(), new DefaultUniqueTicketIdGenerator());
    }

    @BeforeEach
    void onSetUp() throws Exception {
        val context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = getServiceValidateControllerInstance();
        this.serviceValidateController.setApplicationContext(context);

        val requestContext = MockRequestContext.create(context);
        requestContext
            .setRemoteAddr("127.26.152.11")
            .setLocalAddr("109.98.51.12")
            .withUserAgent()
            .setClientInfo();
    }

    protected HttpServletRequest getHttpServletRequest() throws Throwable {
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
    void verifyEmptyParams() throws Throwable {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
            new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    void verifyValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler((credential, __) -> null);
        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    void verifyValidServiceTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        val mv = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(mv.getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketInvalidSpec() throws Throwable {
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyRenewSpecFailsCorrectly() throws Throwable {
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyInvalidServiceTicket() throws Throwable {
        val ctx = CoreAuthenticationTestUtils
            .getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        getTicketRegistry().deleteTicket(tId.getId());

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNotNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    void verifyValidServiceTicketWithValidPgtButNoProxyHandlingBecausePgtIsReleased() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), DEFAULT_SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), DEFAULT_SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, DEFAULT_SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, DEFAULT_SERVICE.getId());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    void verifyValidServiceTicketAndPgtUrlMismatch() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, "http://www.github.com");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_IOU));
    }

    @Test
    void verifyValidServiceTicketAndFormatAsJson() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, ValidationResponseType.JSON.name());

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains("Json"));
    }

    @Test
    void verifyUnknownService() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("unknown-service");
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-123456");
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, GITHUB_URL);

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyDisabledService() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("cas-access-disabled");
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, "ST-123456");
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, GITHUB_URL);

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
    }


    @Test
    void verifyValidServiceTicketAndBadFormat() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("proxyService");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, svc.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_FORMAT, "NOTHING");

        val modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Throwable {
        this.serviceValidateController.addValidationSpecification(new MockValidationSpecification(false));
        assertFalse(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    /*
        CAS10 Proxying Tests.
     */
    @Test
    void verifyValidServiceTicketWithDifferentEncoding() throws Throwable {
        val svc = RegisteredServiceTestUtils.getService("http://www.jasig.org?param=hello+world");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://www.jasig.org?param=hello%20world";
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request,
            new MockHttpServletResponse()).getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketWithSecurePgtUrl() throws Throwable {
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        val modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl(SERVICE);
        assertTrue(Objects.requireNonNull(modelAndView.getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, SERVICE.getId());
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
            .getView()).toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Throwable {
        val origSvc = "http://www.jasig.org?param=hello+world";
        val svc = RegisteredServiceTestUtils.getService(origSvc);
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        val reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService(reqSvc).getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        this.serviceValidateController.getServiceValidateConfigurationContext().setProxyHandler(new Cas10ProxyHandler());
        assertTrue(Objects.requireNonNull(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView())
            .toString().contains(SUCCESS));
    }

    @Test
    void verifyValidServiceTicketWithInvalidPgt() throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

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
    protected ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl(final Service service) throws Throwable {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);
        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET_URL, GITHUB_URL);

        return this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
    }

    public abstract AbstractServiceValidateController getServiceValidateControllerInstance();
}
