package org.apereo.cas.multitenancy;

import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link TenantWebflowDecorator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class TenantWebflowDecorator implements WebflowDecorator {
    private final TenantExtractor tenantExtractor;

    @Override
    public void decorate(final RequestContext requestContext) throws Throwable {
        tenantExtractor.extract(requestContext).ifPresent(tenant ->
            requestContext.getFlowScope().put("tenantUserInterfacePolicy", tenant.getUserInterfacePolicy()));
    }
}
