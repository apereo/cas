package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * This is {@link DetermineMultifactorAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class DetermineMultifactorAuthenticationAction extends AbstractAction {
    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final PrincipalFactory passwordlessPrincipalFactory;

    private final AuthenticationSystemSupport authenticationSystemSupport;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val user = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);

        val service = WebUtils.getService(requestContext);
        val attributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects((Map) user.getAttributes());
        val principal = this.passwordlessPrincipalFactory.createPrincipal(user.getName(), attributes);
        val auth = DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .build();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val result = multifactorTriggerSelectionStrategy.resolve(request, registeredService, auth, service);
        if (result.isPresent()) {
            val builder = new DefaultAuthenticationResultBuilder();
            val authenticationResult = builder
                .collect(auth)
                .build(this.authenticationSystemSupport.getPrincipalElectionStrategy(), service);

            WebUtils.putAuthenticationResultBuilder(builder, requestContext);
            WebUtils.putAuthenticationResult(authenticationResult, requestContext);
            WebUtils.putAuthentication(auth, requestContext);

            return new EventFactorySupport().event(this, result.get());
        }
        return success();
    }
}
