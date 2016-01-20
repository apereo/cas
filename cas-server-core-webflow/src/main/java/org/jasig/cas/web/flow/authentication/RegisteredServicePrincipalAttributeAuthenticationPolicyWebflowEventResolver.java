package org.jasig.cas.web.flow.authentication;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
public class RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver
        extends RegisteredServiceAuthenticationPolicyWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        final RegisteredServiceAuthenticationPolicy policy = service.getAuthenticationPolicy();
        if (service.getAuthenticationPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            logger.debug("Authentication policy does not contain any multifactor authentication providers");
            return null;
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())
                || StringUtils.isBlank(policy.getPrincipalAttributeValueToMatch())) {
            logger.debug("Authentication policy does not define a principal attribute and/or value to trigger multifactor authentication");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        return resolveEventViaPrincipalAttribute(principal, policy.getPrincipalAttributeNameTrigger(), service, context,
                Predicates.containsPattern(policy.getPrincipalAttributeValueToMatch()));
    }
}
