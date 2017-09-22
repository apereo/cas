package org.apereo.cas.web;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockValidationSpecification;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.apereo.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.validation.Cas20ProtocolValidationSpecification;
import org.apereo.cas.validation.Cas20WithoutProxyingValidationSpecification;
import org.apereo.cas.validation.ValidationResponseType;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.junit.Before;
import org.junit.Test;
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
public abstract class AbstractServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTests {

    private static final Service SERVICE = CoreAuthenticationTestUtils.getService();
    private static final String SUCCESS = "Success";
    private static final String SERVICE_PARAM = "service";
    private static final String TICKET_PARAM = "ticket";
    private static final String GITHUB_URL = "https://www.github.com";
    private static final String PGT_URL_PARAM = "pgtUrl";
    private static final String PGT_IOU_PARAM = "pgtIou";

    protected AbstractServiceValidateController serviceValidateController;

    @Before
    public void onSetUp() throws Exception {
        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController.setCentralAuthenticationService(getCentralAuthenticationService());
        this.serviceValidateController.setAuthenticationSystemSupport(getAuthenticationSystemSupport());
        final Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler(new SimpleHttpClientFactoryBean().getObject(), new DefaultUniqueTicketIdGenerator());
        this.serviceValidateController.setProxyHandler(proxyHandler);
        this.serviceValidateController.setApplicationContext(context);
        this.serviceValidateController.setArgumentExtractor(getArgumentExtractor());
        this.serviceValidateController.setServicesManager(getServicesManager());
        this.serviceValidateController.setValidationSpecification(new Cas20WithoutProxyingValidationSpecification());
        this.serviceValidateController.setMultifactorTriggerSelectionStrategy(new DefaultMultifactorTriggerSelectionStrategy("", ""));
    }

    private HttpServletRequest getHttpServletRequest() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);
        final ServiceTicket sId2 = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, null);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId2.getId());
        request.addParameter("renew", "true");

        return request;
    }

    @Test
    public void verifyAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController.setValidationSpecification(new Cas20ProtocolValidationSpecification());
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler(null, null));
    }

    @Test
    public void verifyEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
                new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    public void verifyValidServiceTicket() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());

        final ModelAndView mv = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(mv.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketInvalidSpec() throws Exception {
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.setValidationSpecification(new MockValidationSpecification(false));
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyRenewSpecFailsCorrectly() throws Exception {
        this.serviceValidateController.setValidationSpecification(new Cas20WithoutProxyingValidationSpecification(true));
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyInvalidServiceTicket() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils
                .getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId.getId());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());

        assertFalse(this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, GITHUB_URL);

        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse())
                .getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithSecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final ModelAndView modelAndView = getModelAndViewUponServiceValidationWithSecurePgtUrl();
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithInvalidPgt() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, "duh");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(PGT_IOU_PARAM));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, GITHUB_URL);

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains(SUCCESS));
        assertNotNull(modelAndView.getModel().get(PGT_IOU_PARAM));
    }

    @Test
    public void verifyValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, GITHUB_URL);

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
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(PGT_IOU_PARAM));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncodingAndIgnoringCase() throws Exception {
        final String origSvc = "http://www.jasig.org?param=hello+world";
        final Service svc = CoreAuthenticationTestUtils.getService(origSvc);
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://WWW.JASIG.ORG?PARAM=hello%20world";

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, CoreAuthenticationTestUtils.getService(reqSvc).getId());
        request.addParameter(TICKET_PARAM, sId.getId());

        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketWithDifferentEncoding() throws Exception {
        final Service svc = CoreAuthenticationTestUtils.getService("http://www.jasig.org?param=hello+world");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final String reqSvc = "http://www.jasig.org?param=hello%20world";
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, CoreAuthenticationTestUtils.getService(reqSvc).getId());
        request.addParameter(TICKET_PARAM, sId.getId());

        assertTrue(this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }

    @Test
    public void verifyValidServiceTicketAndPgtUrlMismatch() throws Exception {
        final Service svc = CoreAuthenticationTestUtils.getService("proxyService");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, svc.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, "http://www.github.com");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertFalse(modelAndView.getView().toString().contains(SUCCESS));
        assertNull(modelAndView.getModel().get(PGT_IOU_PARAM));
    }

    @Test
    public void verifyValidServiceTicketAndFormatAsJson() throws Exception {
        final Service svc = CoreAuthenticationTestUtils.getService("proxyService");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, svc.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter("format", ValidationResponseType.JSON.name());

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains("Json"));
    }

    @Test
    public void verifyValidServiceTicketAndBadFormat() throws Exception {
        final Service svc = CoreAuthenticationTestUtils.getService("proxyService");
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), svc);

        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);

        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), svc, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, svc.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter("format", "NOTHING");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(modelAndView.getView().toString().contains("Success"));
    }

    protected ModelAndView getModelAndViewUponServiceValidationWithSecurePgtUrl() throws Exception {
        final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);
        final TicketGrantingTicket tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        final ServiceTicket sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SERVICE_PARAM, SERVICE.getId());
        request.addParameter(TICKET_PARAM, sId.getId());
        request.addParameter(PGT_URL_PARAM, GITHUB_URL);

        return this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
    }
}
