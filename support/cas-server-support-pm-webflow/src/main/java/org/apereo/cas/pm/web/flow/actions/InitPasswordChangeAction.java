package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class InitPasswordChangeAction extends BaseCasWebflowAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val core = casProperties.getAuthn().getPm().getCore();
        WebUtils.putPasswordPolicyPattern(requestContext, core.getPasswordPolicyPattern());
        requestContext.getFlowScope().put("passwordPolicyCharacterSet", core.getPasswordPolicyCharacterSet());
        requestContext.getFlowScope().put("passwordPolicyPasswordLength", core.getPasswordPolicyPasswordLength());

        val upc = getCredentialFromWebflow(requestContext);
        if (upc != null) {
            LOGGER.debug("Restoring credential [{}] for password management", upc);
            WebUtils.putCredential(requestContext, upc);
            PasswordManagementWebflowUtils.putPasswordResetUsername(requestContext, upc.getId());
        }
        return null;
    }

    protected UsernamePasswordCredential getCredentialFromWebflow(final RequestContext requestContext) {
        val attributes = requestContext.getCurrentEvent().getAttributes();
        if (!attributes.isEmpty() && attributes.contains(Credential.class.getName())) {
            return attributes.get(Credential.class.getName(), UsernamePasswordCredential.class);
        }
        return requestContext.getConversationScope().get(Credential.class.getName(), UsernamePasswordCredential.class);
    }
}
