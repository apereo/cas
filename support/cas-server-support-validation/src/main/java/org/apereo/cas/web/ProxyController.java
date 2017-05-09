package org.apereo.cas.web;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The ProxyController is involved with returning a Proxy Ticket (in CAS 2
 * terms) to the calling application. In CAS 3, a Proxy Ticket is just a Service
 * Ticket granted to a service.
 * <p>
 * The ProxyController requires the following property to be set:
 * </p>
 * <ul>
 * <li> centralAuthenticationService - the service layer</li>
 * <li> casArgumentExtractor - the assistant for extracting parameters</li>
 * </ul>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ProxyController extends AbstractDelegateController {

    /**
     * View for if the creation of a "Proxy" Ticket Fails.
     */
    private static final String CONST_PROXY_FAILURE = "cas2ProxyFailureView";

    /**
     * View for if the creation of a "Proxy" Ticket Succeeds.
     */
    private static final String CONST_PROXY_SUCCESS = "cas2ProxySuccessView";

    /**
     * Key to use in model for service tickets.
     */
    private static final String MODEL_SERVICE_TICKET = "ticket";

    private final CentralAuthenticationService centralAuthenticationService;
    private final ServiceFactory webApplicationServiceFactory;

    @Autowired
    private ApplicationContext context;

    /**
     * Instantiates a new proxy controller, with cache seconds set to 0.
     *
     * @param centralAuthenticationService the central authentication service
     * @param webApplicationServiceFactory the web application service factory
     */
    public ProxyController(final CentralAuthenticationService centralAuthenticationService,
                           final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        final String proxyGrantingTicket = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET);
        final Service targetService = getTargetService(request);
        return StringUtils.hasText(proxyGrantingTicket) && targetService != null;
    }

    /**
     * Handle request internal.
     *
     * @param request  the request
     * @param response the response
     * @return ModelAndView containing a view name of either
     * {@code casProxyFailureView} or {@code casProxySuccessView}
     */
    @GetMapping(path = "/proxy")
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        final String proxyGrantingTicket = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET);
        final Service targetService = getTargetService(request);

        if (!StringUtils.hasText(proxyGrantingTicket) || targetService == null) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, null, request);
        }

        try {
            final ProxyTicket proxyTicket = this.centralAuthenticationService.grantProxyTicket(proxyGrantingTicket, targetService);
            return new ModelAndView(CONST_PROXY_SUCCESS, MODEL_SERVICE_TICKET, proxyTicket);
        } catch (final AbstractTicketException e) {
            return generateErrorView(e.getCode(), new Object[]{proxyGrantingTicket}, request);
        } catch (final UnauthorizedServiceException e) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY, new Object[]{targetService}, request);
        }
    }

    /**
     * Gets the target service from the request.
     *
     * @param request the request
     * @return the target service
     */
    private Service getTargetService(final HttpServletRequest request) {
        return this.webApplicationServiceFactory.createService(request);
    }

    /**
     * Generate error view stuffing the code and description
     * of the error into the model. View name is set to {@link #CONST_PROXY_FAILURE}.
     *
     * @param code the code
     * @param args the msg args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final Object[] args, final HttpServletRequest request) {
        final ModelAndView modelAndView = new ModelAndView(CONST_PROXY_FAILURE);
        modelAndView.addObject("code", StringEscapeUtils.escapeHtml4(code));
        modelAndView.addObject("description", StringEscapeUtils.escapeHtml4(this.context.getMessage(code, args, code, request.getLocale())));
        return modelAndView;
    }

    public void setApplicationContext(final ApplicationContext context) {
        this.context = context;
    }
}
