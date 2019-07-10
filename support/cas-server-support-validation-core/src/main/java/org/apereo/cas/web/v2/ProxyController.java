package org.apereo.cas.web.v2;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.AbstractDelegateController;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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
@Setter
@Getter
@RequiredArgsConstructor
public class ProxyController extends AbstractDelegateController {

    /**
     * The view to redirect to on a successful validation.
     */
    private final View successView;

    /**
     * The view to redirect to on a validation failure.
     */
    private final View failureView;

    private final CentralAuthenticationService centralAuthenticationService;

    private final ServiceFactory webApplicationServiceFactory;

    private final ApplicationContext context;

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        val proxyGrantingTicket = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET);
        val targetService = getTargetService(request);
        return targetService != null && StringUtils.hasText(proxyGrantingTicket);
    }

    @Override
    @GetMapping(path = "/proxy")
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        val proxyGrantingTicket = request.getParameter(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET);
        val targetService = getTargetService(request);
        if (!StringUtils.hasText(proxyGrantingTicket) || targetService == null) {
            return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST_PROXY, null, request);
        }
        try {
            val proxyTicket = this.centralAuthenticationService.grantProxyTicket(proxyGrantingTicket, targetService);
            val model = CollectionUtils.wrap(CasProtocolConstants.PARAMETER_TICKET, proxyTicket);
            return new ModelAndView(this.successView, (Map) model);
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
     * of the error into the model. View name is set to {@link #failureView}.
     *
     * @param code the code
     * @param args the msg args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final Object[] args, final HttpServletRequest request) {
        val modelAndView = new ModelAndView(this.failureView);
        modelAndView.addObject("code", StringEscapeUtils.escapeHtml4(code));
        val desc = StringEscapeUtils.escapeHtml4(this.context.getMessage(code, args, code, request.getLocale()));
        modelAndView.addObject("description", desc);
        return modelAndView;
    }
}
