package org.apereo.cas.adaptors.duo.web.flow.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;

import static org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA;
import static org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER;

/**
 * This is {@link DetermineDuoFailureAction}.
 *
 * @author Travis Schmidt
 * @since 5.3.3
 */
@Slf4j
@RequiredArgsConstructor
public class DetermineDuoFailureAction extends AbstractAction {
    private final VariegatedMultifactorAuthenticationProvider provider;
    private final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Principal p = authentication.getPrincipal();
        final RegisteredService service = WebUtils.getRegisteredService(requestContext);

        final Collection<String> providerIds = WebUtils.getResolvedMultifactorAuthenticationProviders(requestContext);
        final Collection<MultifactorAuthenticationProvider> providers =
                MultifactorAuthenticationUtils.getMultifactorAuthenticationProvidersByIds(providerIds, applicationContext);

        final Event unavailable = new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        for (final MultifactorAuthenticationProvider pr : providers) {
            final DuoMultifactorAuthenticationProvider duoProvider = this.provider.findProvider(pr.getId(), DuoMultifactorAuthenticationProvider.class);
            final RegisteredServiceMultifactorPolicy.FailureModes failureMode = duoProvider.determineFailureMode(service);
            if (failureMode != RegisteredServiceMultifactorPolicy.FailureModes.CLOSED) {
                authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
                authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, duoProvider.getId());
            } else {
                return unavailable;
            }

        }
        return success();
    }
}
