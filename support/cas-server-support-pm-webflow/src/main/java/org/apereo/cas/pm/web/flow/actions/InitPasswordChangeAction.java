package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitPasswordChangeAction}, serves a as placeholder for extensions.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class InitPasswordChangeAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        WebUtils.putPasswordPolicyPattern(requestContext,
            casProperties.getAuthn().getPm().getCore().getPasswordPolicyPattern());
        val attributes = requestContext.getCurrentEvent().getAttributes();
        if (!attributes.isEmpty() && attributes.contains(Credential.class.getName())) {
            val upc = attributes.get(Credential.class.getName(), UsernamePasswordCredential.class);
            LOGGER.debug("Restoring credential [{}] for password management", upc);
            WebUtils.putCredential(requestContext, upc);
        }
        return null;
    }
}
