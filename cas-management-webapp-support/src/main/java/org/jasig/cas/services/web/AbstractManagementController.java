package org.jasig.cas.services.web;

import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.web.view.JsonViewUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * Parent controller for all views.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractManagementController {
    /** Ajax request header name to examine for exceptions. */
    private static final String AJAX_REQUEST_HEADER_NAME = "x-requested-with";

    /** Ajax request header value to examine for exceptions. */
    private static final String AJAX_REQUEST_HEADER_VALUE = "XMLHttpRequest";

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Instance of ServicesManager. */
    @NotNull
    protected final ReloadableServicesManager servicesManager;

    /**
     * Instantiates a new manage registered services multi action controller.
     *
     * @param servicesManager the services manager
     */
    public AbstractManagementController(final ReloadableServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * Resolve exception.
     *
     * @param request the request
     * @param response the response
     * @param ex the exception
     * @return the model and view
     * @throws IOException the iO exception
     */
    @ExceptionHandler
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
                                         final Exception ex) throws IOException {

        logger.error(ex.getMessage(), ex);
        final String contentType = request.getHeader(AJAX_REQUEST_HEADER_NAME);
        if (contentType != null && contentType.equals(AJAX_REQUEST_HEADER_VALUE)) {
            logger.debug("Handling exception {} for ajax request indicated by header {}",
                    ex.getClass().getName(), AJAX_REQUEST_HEADER_NAME);
            JsonViewUtils.renderException(ex, response);
            return null;
        } else {
            logger.trace("Unable to resolve exception {} for request. Ajax request header {} not found.",
                    ex.getClass().getName(), AJAX_REQUEST_HEADER_NAME);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            final ModelAndView mv = new ModelAndView("errors");
            mv.addObject(ex);
            return mv;
        }
    }
}
