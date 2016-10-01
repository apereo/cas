package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.authn.api.DuoApiCredential;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoNonWebAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoNonWebAuthenticationAction extends AbstractAction {
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final DuoApiCredential c = new DuoApiCredential(WebUtils.getAuthentication(requestContext));
        WebUtils.putCredential(requestContext, c);
        return success();
    }

}
