package org.apereo.cas.web.flow.action;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateSelectionAction extends AbstractAction {
    private final String separator;

    public SurrogateSelectionAction(final String separator) {
        this.separator = separator;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Credential credential = WebUtils.getCredential(requestContext);
        if (credential instanceof UsernamePasswordCredential) {
            final UsernamePasswordCredential upc = UsernamePasswordCredential.class.cast(credential);
            final String target = requestContext.getExternalContext().getRequestParameterMap().get("surrogateTarget");

            if (StringUtils.isNotBlank(target)) {
                upc.setUsername(target + this.separator + upc.getUsername());
            }
        }
        return success();
    }
}
