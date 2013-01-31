package org.jasig.cas.web.view;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.web.servlet.view.json.JsonExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Implementation of JsonView's exception handler that provides an XML-escaped response to prevent cross-site scripting attacks.
 *
 * @author Scott Battaglia
 * @since 4.0.0
 */
public final class HtmlEscapedExceptionMessageExceptionHandler implements JsonExceptionHandler {

    public static final String MESSAGE_MODEL_KEY = "exception.message";

    @NotNull
    private String modelKey = MESSAGE_MODEL_KEY;

    @Override
    public void triggerException(final Exception e, final Map model, final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws Exception {
        model.put(this.modelKey, StringEscapeUtils.escapeHtml(e.getMessage()));
    }

    public void setModelKey(final String modelKey) {
        this.modelKey = modelKey;
    }
}
