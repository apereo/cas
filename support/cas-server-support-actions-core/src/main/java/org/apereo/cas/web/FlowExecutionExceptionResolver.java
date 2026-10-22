package org.apereo.cas.web;

import module java.base;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.FlowExecutionRepositoryException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@link FlowExecutionExceptionResolver} catches the {@link FlowExecutionRepositoryException}
 * thrown by Spring Webflow when the given flow id no longer exists. This can
 * occur if a particular flow has reached an end state (the id is no longer valid)
 * <p>
 * It will redirect back to the requested URI which should start a new workflow.
 * </p>
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Slf4j
@Getter
public class FlowExecutionExceptionResolver implements HandlerExceptionResolver {
    private static final String PARAMETER_EXECUTION = "execution";

    private final String modelKey = "exception.message";

    /**
     * Since FlowExecutionRepositoryException is a common ancestor to these exceptions and other
     * error cases we would likely want to hide from the user, it seems reasonable to check for
     * FlowExecutionRepositoryException, and restart the flow by redirecting to the requested URI.
     * <p>
     * The {@code execution} parameter is dropped from the query string of that redirect. The execution
     * it carries could not be used, and keeping it would send a badly formatted key straight back,
     * redirecting forever; dropping it lets {@link BadlyFormattedFlowExecutionKeyException} restart the
     * flow like any other repository error instead of surfacing as a server error.
     *
     * @param request   the request
     * @param response  the response
     * @param handler   the handler
     * @param exception the exception
     * @return the redirect that restarts the flow, or null if the exception is not handled here
     */
    @Override
    public ModelAndView resolveException(
        @NonNull
        final HttpServletRequest request,
        @NonNull
        final HttpServletResponse response, final Object handler,
        @NonNull
        final Exception exception) {

        if (!(exception instanceof FlowExecutionRepositoryException)) {
            LOGGER.debug("Ignoring the received exception [{}] due to a type mismatch with handler [{}]", exception, handler);
            return null;
        }

        val queryString = Optional.ofNullable(request.getQueryString())
            .stream()
            .flatMap(query -> Arrays.stream(query.split("&")))
            .filter(parameter -> !parameter.equals(PARAMETER_EXECUTION) && !parameter.startsWith(PARAMETER_EXECUTION + '='))
            .collect(Collectors.joining("&"));
        val urlToRedirectTo = request.getRequestURI() + (queryString.isEmpty() ? StringUtils.EMPTY : '?' + queryString);

        LOGGER.debug("Error getting flow information for URL [{}]", urlToRedirectTo, exception);
        val model = new HashMap<String, Object>();
        model.put(this.modelKey, StringEscapeUtils.escapeHtml4(exception.getMessage()));

        return new ModelAndView(new RedirectView(urlToRedirectTo), model);
    }
}
