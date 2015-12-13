package org.jasig.cas.web;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

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
@Component("proxyController")
@Controller
public class ProxyController {

    /** View for if the creation of a "Proxy" Ticket Fails. */
    private static final String CONST_PROXY_FAILURE = "cas2ProxyFailureView";

    /** View for if the creation of a "Proxy" Ticket Succeeds. */
    private static final String CONST_PROXY_SUCCESS = "cas2ProxySuccessView";

    /** Key to use in model for service tickets. */
    private static final String MODEL_SERVICE_TICKET = "ticket";

    /** CORE to delegate all non-web tier functionality to. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private ApplicationContext context;

    /**
     * Instantiates a new proxy controller, with cache seconds set to 0.
     */
    public ProxyController() {}

    /**
     * Handle request internal.
     *
     * @param request the request
     * @param response the response
     * @return ModelAndView containing a view name of either
     * {@code casProxyFailureView} or {@code casProxySuccessView}
     */
    @RequestMapping(path="/proxy", method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        final String proxyGrantingTicket = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTINOG_TICKET);
        final Service targetService = getTargetService(request);

        if (!StringUtils.hasText(proxyGrantingTicket) || targetService == null) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST,
                CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, null, request);
        }

        try {
            final ProxyTicket proxyTicket = this.centralAuthenticationService.grantProxyTicket(proxyGrantingTicket, targetService);
            return new ModelAndView(CONST_PROXY_SUCCESS, MODEL_SERVICE_TICKET, proxyTicket);
        } catch (final AbstractTicketException e) {
            return generateErrorView(e.getCode(), e.getCode(), new Object[] {proxyGrantingTicket}, request);
        } catch (final UnauthorizedServiceException e) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE,
                CasProtocolConstants.ERROR_CODE_UNAUTHORIZED_SERVICE_PROXY,
                new Object[] {targetService}, request);
        }
    }

    /**
     * Gets the target service from the request.
     *
     * @param request the request
     * @return the target service
     */
    private static Service getTargetService(final HttpServletRequest request) {
        return new WebApplicationServiceFactory().createService(request);
    }

    /**
     * Generate error view stuffing the code and description
     * of the error into the model. View name is set to {@link #CONST_PROXY_FAILURE}.
     *
     * @param code the code
     * @param description the description
     * @param args the msg args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code,
        final String description, final Object[] args, final HttpServletRequest request) {
        final ModelAndView modelAndView = new ModelAndView(CONST_PROXY_FAILURE);
        modelAndView.addObject("code", code);
        modelAndView.addObject("description", this.context.getMessage(description, args,
            description, request.getLocale()));

        return modelAndView;
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    @Autowired
    public void setCentralAuthenticationService(
        @Qualifier("centralAuthenticationService")
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setApplicationContext(final ApplicationContext context) {
        this.context = context;
    }
}
