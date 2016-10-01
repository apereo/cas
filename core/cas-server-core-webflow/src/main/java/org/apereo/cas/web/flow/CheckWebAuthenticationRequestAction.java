package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link CheckWebAuthenticationRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CheckWebAuthenticationRequestAction extends AbstractAction {
    private String contentType;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        
        if (this.contentType.equalsIgnoreCase(request.getContentType())) {
            return new EventFactorySupport().no(this);
        }
        return new EventFactorySupport().yes(this);
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
}
