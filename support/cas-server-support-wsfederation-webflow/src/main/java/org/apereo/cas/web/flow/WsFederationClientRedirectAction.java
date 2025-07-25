package org.apereo.cas.web.flow;


import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WsFederationClientRedirectAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class WsFederationClientRedirectAction extends BaseCasWebflowAction {
    private final ServerProperties serverProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val clients = WebUtils.getWsFederationDelegatedClients(requestContext, WsFedClient.class);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        clients
            .stream()
            .filter(client -> client.getAutoRedirectType() == DelegationAutoRedirectTypes.SERVER)
            .findFirst()
            .ifPresent(Unchecked.consumer(client -> {
                val url = Strings.CI.prependIfMissing(serverProperties.getServlet().getContextPath(), "/")
                          + Strings.CI.prependIfMissing(client.getRedirectUrl(), "/");
                LOGGER.debug("Redirecting to [{}] for WS client [{}]", url, client.getName());
                response.sendRedirect(url);
            }));
        return null;
    }
}
