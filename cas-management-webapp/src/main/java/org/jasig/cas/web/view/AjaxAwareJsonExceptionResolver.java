package org.jasig.cas.web.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.exception.JsonExceptionResolver;

/**
* Implementation of JsonExceptionResolver that only triages the exception occurred
* for json requests.
*
* @author Misagh Moayyed
* @since 4.0.0
*/
public class AjaxAwareJsonExceptionResolver extends JsonExceptionResolver {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String ajaxRequestHeaderName ="x-requested-with";
    private String ajaxRequestHeaderValue = "XMLHttpRequest";

    public void setAjaxRequestHeaderName(final String ajaxRequestHeaderName) {
        this.ajaxRequestHeaderName = ajaxRequestHeaderName;
    }

    public void setAjaxRequestHeaderValue(final String ajaxRequestHeaderValue) {
        this.ajaxRequestHeaderValue = ajaxRequestHeaderValue;
    }

    @Override
    public ModelAndView resolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        final String contentType = request.getHeader(this.ajaxRequestHeaderName);
        if (contentType.equals(this.ajaxRequestHeaderValue)) {
            log.debug("Handling exception {} for ajax request indicated by header {}", ex.getClass().getName(), this.ajaxRequestHeaderName);
            return super.resolveException(request, response, handler, ex);
        }
        return null;
    }

}
