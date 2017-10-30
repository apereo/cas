package org.apereo.cas.mgmt.services.web;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Parent controller for all views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractManagementController {
    /**
     * Ajax request header name to examine for exceptions.
     */
    private static final String AJAX_REQUEST_HEADER_NAME = "x-requested-with";

    /**
     * Ajax request header value to examine for exceptions.
     */
    private static final String AJAX_REQUEST_HEADER_VALUE = "XMLHttpRequest";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractManagementController.class);

    /**
     * Instance of ServicesManager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager the services manager
     */
    public AbstractManagementController(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Resolve exception.
     *
     * @param request  the request
     * @param response the response
     * @param ex       the exception
     * @return the model and view
     */
    @ExceptionHandler
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response, final Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        final String contentType = request.getHeader(AJAX_REQUEST_HEADER_NAME);
        if (contentType != null && contentType.equals(AJAX_REQUEST_HEADER_VALUE)) {
            LOGGER.debug("Handling exception [{}] for ajax request indicated by header [{}]",
                    ex.getClass().getName(), AJAX_REQUEST_HEADER_NAME);
            JsonUtils.renderException(ex, response);
            return null;
        }
        LOGGER.trace("Unable to resolve exception [{}] for request. AJAX request header [{}] not found.",
                ex.getClass().getName(), AJAX_REQUEST_HEADER_NAME);
        final ModelAndView mv = new ModelAndView("error");
        mv.addObject(ex);
        return mv;
    }
}
