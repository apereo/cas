package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegating controller.
 * Tries to find a controller among its delegates, that can handle the current request.
 * If none is found, an error is generated.
 *
 * @author Frederic Esnault
 * @deprecated 6.2
 * @since 3.5
 */
@Setter
@Deprecated(since = "6.2.0")
@Getter
public class DelegatingController extends AbstractController {

    /**
     * View if Service Ticket Validation Fails.
     */
    private static final String DEFAULT_ERROR_VIEW_NAME = "casServiceFailureView";

    private List<AbstractDelegateController> delegates = new ArrayList<>();

    /**
     * The view to redirect if no delegate can handle the request.
     */
    private String failureView = DEFAULT_ERROR_VIEW_NAME;

    /**
     * Handles the request.
     * Ask all delegates if they can handle the current request.
     * The first to answer true is elected as the delegate that will process the request.
     * If no controller answers true, we redirect to the error page.
     *
     * @param request  the request to handle
     * @param response the response to write to
     * @return the model and view object
     * @throws Exception if an error occurs during request handling
     */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        for (val delegate : this.delegates) {
            if (delegate.canHandle(request, response)) {
                return delegate.handleRequestInternal(request, response);
            }
        }
        return generateErrorView(CasProtocolConstants.ERROR_CODE_INVALID_REQUEST, null);
    }

    /**
     * Generate error view based on {@link #setFailureView(String)}.
     *
     * @param code the code
     * @param args the args
     * @return the model and view
     */
    private ModelAndView generateErrorView(final String code, final Object[] args) {
        val modelAndView = new ModelAndView(this.failureView);
        val convertedDescription = getMessageSourceAccessor().getMessage(code, args, code);
        modelAndView.addObject("code", StringEscapeUtils.escapeHtml4(code));
        modelAndView.addObject("description", StringEscapeUtils.escapeHtml4(convertedDescription));
        return modelAndView;
    }
}
