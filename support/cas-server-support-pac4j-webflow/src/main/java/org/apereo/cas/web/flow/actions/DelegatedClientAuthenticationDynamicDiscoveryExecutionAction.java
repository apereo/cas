package org.apereo.cas.web.flow.actions;

import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
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
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val userid = requestContext.getRequestParameters().get("username");
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(userid)
            .build();
        val client = selector.locate(request);
        if (client.isEmpty()) {
            val msg = new MessageBuilder()
                .error()
                .code("screen.pac4j.discovery.unknownclient")
                .build();
            requestContext.getMessageContext().addMessage(msg);
            requestContext.getRequestScope().put("username", userid);
            return error();
        }
        configContext.getDelegatedClientIdentityProvidersProducer().produce(requestContext, client.get())
            .ifPresent(cfg -> requestContext.getRequestScope()
                .put(DelegatedAuthenticationDynamicDiscoveryProviderLocator.REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL, cfg.getRedirectUrl()));
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT,
            new LocalAttributeMap<>("client", client.get()));
    }
}
