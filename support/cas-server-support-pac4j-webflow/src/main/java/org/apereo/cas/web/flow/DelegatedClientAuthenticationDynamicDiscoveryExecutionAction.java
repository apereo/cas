package org.apereo.cas.web.flow;

import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
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
public class DelegatedClientAuthenticationDynamicDiscoveryExecutionAction extends AbstractAction {
    /**
     * Attribute name in the request scope to indicate the direct url.
     */
    public static final String REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL = "delegatedAuthProviderRedirectUrl";

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
                .put(REQUEST_SCOPE_ATTR_PROVIDER_REDIRECT_URL, cfg.getRedirectUrl()));
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT,
            new LocalAttributeMap<>("client", client.get()));
    }
}
