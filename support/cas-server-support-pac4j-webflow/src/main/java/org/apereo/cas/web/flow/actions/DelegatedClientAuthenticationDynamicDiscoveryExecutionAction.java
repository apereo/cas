package org.apereo.cas.web.flow.actions;

import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationDynamicDiscoveryExecutionAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DelegatedClientAuthenticationDynamicDiscoveryExecutionAction extends BaseCasWebflowAction {
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    private final DelegatedAuthenticationDynamicDiscoveryProviderLocator selector;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val userid = requestContext.getRequestParameters().get("username");
        val discoveryRequest = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(userid)
            .build();

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        
        val client = FunctionUtils.doUnchecked(() -> selector.locate(discoveryRequest, webContext));
        if (client.isEmpty()) {
            WebUtils.addErrorMessageToContext(requestContext, "screen.pac4j.discovery.unknownclient");
            requestContext.getRequestScope().put("username", userid);
            return error();
        }
        return FunctionUtils.doUnchecked(() -> {
            configContext.getDelegatedClientIdentityProvidersProducer().produce(requestContext, client.get())
                .ifPresent(cfg -> requestContext.getRequestScope()
                    .put(DelegatedAuthenticationDynamicDiscoveryProviderLocator.REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL, cfg.getRedirectUrl()));
            return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT,
                new LocalAttributeMap<>("client", client.get()));
        });
    }
}
