package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * This is {@link WsFederationRequestBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WsFederationRequestBuilder {
    /**
     * Flow scope parameter pointing to client instances.
     */
    public static final String PARAMETER_NAME_WSFED_CLIENTS = "wsfedUrls";

    private final Collection<WsFederationConfiguration> configurations;
    private final WsFederationHelper wsFederationHelper;

    /**
     * Build authentication request event event.
     *
     * @param context the context
     * @return the event
     */
    public Event buildAuthenticationRequestEvent(final RequestContext context) {
        final List<WsFedClient> clients = new ArrayList<>();
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final var service = (Service) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        this.configurations.forEach(cfg -> {
            final var c = new WsFedClient();
            c.setName(cfg.getName());
            final var id = UUID.randomUUID().toString();
            final var rpId = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
            c.setAuthorizationUrl(cfg.getAuthorizationUrl(rpId, id));
            c.setReplyingPartyId(rpId);
            c.setId(id);
            c.setRedirectUrl(getRelativeRedirectUrlFor(cfg, service, request));
            c.setAutoRedirect(cfg.isAutoRedirect());
            clients.add(c);
        });
        context.getFlowScope().put(PARAMETER_NAME_WSFED_CLIENTS, clients);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }

    /**
     * Gets redirect url for.
     *
     * @param config  the config
     * @param service the service
     * @param request the request
     * @return the redirect url for
     */
    @SneakyThrows
    private static String getRelativeRedirectUrlFor(final WsFederationConfiguration config, final Service service, final HttpServletRequest request) {
        final var builder = new URIBuilder(WsFederationNavigationController.ENDPOINT_REDIRECT);
        builder.addParameter(WsFederationNavigationController.PARAMETER_NAME, config.getId());
        if (service != null) {
            builder.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
        final var method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(method)) {
            builder.addParameter(CasProtocolConstants.PARAMETER_METHOD, method);
        }
        return builder.toString();
    }
}
