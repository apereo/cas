package org.apereo.cas.web.flow.configurer;

import module java.base;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.expression.Expression;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ExternalRedirectAction}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class ExternalRedirectAction extends AbstractAction {
    private final Expression resourceUri;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        val location = (String) this.resourceUri.getValue(context);
        if (StringUtils.isNotBlank(location)) {
            if (location.startsWith("servletRelative:") || location.startsWith("contextRelative:")
                || location.startsWith("serverRelative:") || location.startsWith("http://")
                || location.startsWith("https://")) {
                context.getExternalContext().requestExternalRedirect(location);
            } else {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.setHeader("Location", location);
                context.getExternalContext().recordResponseComplete();
            }
        }
        return this.success();
    }
}
