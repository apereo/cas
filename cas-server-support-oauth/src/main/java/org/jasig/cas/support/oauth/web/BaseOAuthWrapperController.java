package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * It finds the right sub controller to call according to the url.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("baseOAuthWrapperController")
public abstract class BaseOAuthWrapperController extends AbstractController {

    /** The logger. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** The login url. */
    @NotNull
    @Value("${server.prefix:http://localhost:8080/cas}/login")
    protected String loginUrl;

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /** The ticket registry. */
    @NotNull
    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    /** The timeout. */
    @NotNull
    @Value("${tgt.timeToKillInSeconds:7200}")
    protected long timeout;

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final String method = getMethod(request);
        logger.debug("method : {}", method);
        return internalHandleRequest(method, request, response);
    }

    /**
     * Internal handle request.
     *
     * @param method the method
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    protected abstract ModelAndView internalHandleRequest(String method, HttpServletRequest request,
            HttpServletResponse response) throws Exception;

    /**
     * Return the method to call according to the url.
     *
     * @param request the incoming http request
     * @return the method to call according to the url
     */
    private String getMethod(final HttpServletRequest request) {
        String method = request.getRequestURI();
        if (method.indexOf('?') >= 0) {
            method = StringUtils.substringBefore(method, "?");
        }
        final int pos = method.lastIndexOf('/');
        if (pos >= 0) {
            method = method.substring(pos + 1);
        }
        return method;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public TicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
}
