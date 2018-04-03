package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final Service service = (Service) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        this.configurations.forEach(cfg -> {
            final WsFedClient c = new WsFedClient();
            c.setName(cfg.getName());
            final String id = UUID.randomUUID().toString();
            final String rpId = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
            c.setAuthorizationUrl(cfg.getAuthorizationUrl(rpId, id));
            c.setReplyingPartyId(rpId);
            c.setId(id);
            c.setRedirectUrl(WsFederationNavigationController.getRelativeRedirectUrlFor(cfg, service, request));
            c.setAutoRedirect(cfg.isAutoRedirect());
            clients.add(c);
        });
        context.getFlowScope().put("wsfedUrls", clients);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }
}
