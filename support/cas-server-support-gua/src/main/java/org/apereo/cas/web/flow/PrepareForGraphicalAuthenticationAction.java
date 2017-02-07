package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForGraphicalAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PrepareForGraphicalAuthenticationAction extends AbstractAction {
    private final GraphicalUserAuthenticationProperties guaProperties;

    public PrepareForGraphicalAuthenticationAction(final GraphicalUserAuthenticationProperties guaProperties) {
        this.guaProperties = guaProperties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("guaEnabled", StringUtils.isNotBlank(guaProperties.getImageAttribute()));
        return success();
    }
}
