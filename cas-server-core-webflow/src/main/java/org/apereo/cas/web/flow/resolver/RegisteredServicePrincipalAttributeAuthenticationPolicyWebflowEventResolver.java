package org.apereo.cas.web.flow.resolver;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver}
 * that attempts to locate the given principal attribute in the service authentication policy
 * and match it against the pattern provided in the same policy.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
public class RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver
        extends RegisteredServiceAuthenticationPolicyWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy == null || service.getMultifactorPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            logger.debug("Authentication policy is absent or does not contain any multifactor authentication providers");
            return null;
        }

        if (StringUtils.isBlank(policy.getPrincipalAttributeNameTrigger())
                || StringUtils.isBlank(policy.getPrincipalAttributeValueToMatch())) {
            logger.debug("Authentication policy does not define a principal attribute and/or value to trigger multifactor authentication");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        final Set<MultifactorAuthenticationProvider> providers = getAuthenticationProviderForService(service);
        return resolveEventViaPrincipalAttribute(principal,
                org.springframework.util.StringUtils.commaDelimitedListToSet(policy.getPrincipalAttributeNameTrigger()),
                service, context, providers, Predicates.containsPattern(policy.getPrincipalAttributeValueToMatch()));
    }
}
