package org.jasig.cas.web.flow.resolver;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link PrincipalAttributeAuthenticationPolicyWebflowEventResolver}
 * that attempts to locate a principal attribute, match its value against
 * the provided pattern and decide the next event in the flow for the given service.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("principalAttributeAuthenticationPolicyWebflowEventResolver")
public class PrincipalAttributeAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Value("${cas.mfa.principal.attribute:}")
    private String attributeName;

    @Value("${cas.mfa.principal.attribute.value:}")
    private String attributeValue;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (StringUtils.isBlank(this.attributeName) || StringUtils.isBlank(this.attributeValue)) {
            logger.debug("Attribute name/value to determine event is not configured for {}", principal.getId());
            return null;
        }

        return resolveEventViaPrincipalAttribute(principal, this.attributeName, service, context,
                Predicates.containsPattern(this.attributeValue));
    }
}
