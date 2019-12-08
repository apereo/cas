package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class that provides the doPreExecute() hook to set the find the provider for this webflow to be used by
 * extending classes in doExecute().
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@RequiredArgsConstructor
public abstract class AbstractMultifactorAuthenticationAction<T extends MultifactorAuthenticationProvider> extends AbstractAction {
    /**
     * The resolved provider for this flow.
     */
    protected transient T provider;

    private final transient ApplicationContext applicationContext;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) {
        val providerId = requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID, String.class);
        provider = (T) MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext)
            .orElseThrow(AuthenticationException::new);
        return null;
    }
}
