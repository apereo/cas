package org.apereo.cas.web.flow;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SqrlCleanUpAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlCleanUpAction extends AbstractAction {
    private final SqrlConfig sqrlConfig;
    private final SqrlServerOperations sqrlServerOperations;

    public SqrlCleanUpAction(final SqrlConfig sqrlConfig, final SqrlServerOperations sqrlServerOperations) {
        this.sqrlConfig = sqrlConfig;
        this.sqrlServerOperations = sqrlServerOperations;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        sqrlServerOperations.cleanSqrlAuthData(request, response);
        return success();
    }
}
