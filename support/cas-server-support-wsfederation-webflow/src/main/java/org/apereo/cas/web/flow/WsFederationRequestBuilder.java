package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * This is {@link WsFederationRequestBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class WsFederationRequestBuilder {
    private final Collection<WsFederationConfiguration> configurations;

    private final WsFederationHelper wsFederationHelper;

    private static String getRelativeRedirectUrlFor(final WsFederationConfiguration config,
                                                    final WebApplicationService service,
                                                    final HttpServletRequest request) throws Exception {
        val builder = new URIBuilder(WsFederationNavigationController.ENDPOINT_REDIRECT);
        builder.addParameter(WsFederationNavigationController.PARAMETER_NAME, config.getId());
        if (service != null) {
            builder.addParameter(service.getSource(), service.getId());
        }
        val method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        if (StringUtils.isNotBlank(method)) {
            builder.addParameter(CasProtocolConstants.PARAMETER_METHOD, method);
        }
        return builder.toString();
    }

    /**
     * Build authentication request event event.
     *
     * @param context the context
     * @return the event
     */
    public Event buildAuthenticationRequestEvent(final RequestContext context) {
        val clients = new ArrayList<WsFedClient>(this.configurations.size());
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val service = (WebApplicationService) context.getFlowScope().get(CasProtocolConstants.PARAMETER_SERVICE);
        configurations.forEach(Unchecked.consumer(cfg -> {
            val c = new WsFedClient();
            c.setName(cfg.getName());
            val id = UUID.randomUUID().toString();
            val rpId = wsFederationHelper.getRelyingPartyIdentifier(service, cfg);
            c.setAuthorizationUrl(cfg.getAuthorizationUrl(rpId, id));
            c.setReplyingPartyId(rpId);
            c.setId(id);
            c.setRedirectUrl(getRelativeRedirectUrlFor(cfg, service, request));
            c.setAutoRedirectType(cfg.getAutoRedirectType());
            clients.add(c);

            if (cfg.getAutoRedirectType() != DelegationAutoRedirectTypes.NONE) {
                WebUtils.putDelegatedAuthenticationProviderPrimary(context, cfg);
            }
        }));
        WebUtils.putWsFederationDelegatedClients(context, clients);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROCEED);
    }
}
