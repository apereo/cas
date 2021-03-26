package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import com.duosecurity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.LinkedHashMap;

/**
 * This is {@link DuoSecurityUniversalPromptPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DuoSecurityUniversalPromptPrepareLoginAction extends AbstractMultifactorAuthenticationAction<DuoSecurityMultifactorAuthenticationProvider> {
    private final TicketRegistry ticketRegistry;

    private final MultifactorAuthenticationProviderBean<
        DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean;

    private final TicketFactory ticketFactory;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getInProgressAuthentication();
        val duoSecurityIdentifier = WebUtils.getMultifactorAuthenticationProviderById(requestContext);
        val provider = duoProviderBean.getProvider(duoSecurityIdentifier);

        val client = provider.getDuoAuthenticationService()
            .getDuoClient()
            .map(c -> (Client) c)
            .orElseThrow(() -> new RuntimeException("Unable to locate Duo Security client"));
        val state = client.generateState();

        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);

        val properties = new LinkedHashMap<String, Object>();
        properties.put("duoProviderId", duoSecurityIdentifier);
        properties.put("authentication", authentication);
        properties.put("authenticationResultBuilder", WebUtils.getAuthenticationResultBuilder(requestContext));
        properties.put("authenticationResult", WebUtils.getAuthenticationResult(requestContext));
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null) {
            properties.put("registeredService", registeredService);
        }
        val service = WebUtils.getService(requestContext);
        val ticket = factory.create(state, service, properties);
        ticketRegistry.addTicket(ticket);
        LOGGER.debug("Stored Duo Security session via [{}]", ticket);

        val principal = resolvePrincipal(authentication.getPrincipal());
        val authUrl = client.createAuthUrl(principal.getId(), ticket.getId());
        requestContext.getFlowScope().put("duoUniversalPromptLoginUrl", authUrl);
        LOGGER.debug("Redirecting to Duo Security url at [{}]", authUrl);
        return success(ticket);
    }
}
