package org.apereo.cas.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class SurrogateSelectionAction extends AbstractAction {
    /**
     * Surrogate Target parameter name.
     */
    public static final String PARAMETER_NAME_SURROGATE_TARGET = "surrogateTarget";

    private final String separator;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Credential credential = WebUtils.getCredential(requestContext);
        if (credential instanceof UsernamePasswordCredential) {
            final UsernamePasswordCredential upc = UsernamePasswordCredential.class.cast(credential);
            final String target = requestContext.getExternalContext().getRequestParameterMap().get(PARAMETER_NAME_SURROGATE_TARGET);

            LOGGER.debug("Located surrogate target as [{}]", target);
            if (StringUtils.isNotBlank(target)) {
                upc.setUsername(target + this.separator + upc.getUsername());
                LOGGER.debug("Determined webflow credential id to be [{}]", upc.getUsername());
            }
        } else {
            LOGGER.debug("Current credential in the webflow is not one of [{}]", UsernamePasswordCredential.class.getName());
        }
        return success();
    }
}
