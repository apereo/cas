package org.apereo.cas.web;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.util.Pac4jUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DelegatedAuthenticationWebApplicationServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedAuthenticationWebApplicationServiceFactory extends WebApplicationServiceFactory {
    private final Clients clients;
    private final DelegatedClientWebflowManager delegatedClientWebflowManager;

    @Override
    protected String getRequestedService(final HttpServletRequest request) {
        val service = super.getRequestedService(request);
        if (StringUtils.isNotBlank(service)) {
            return service;
        }

        val clientName = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        LOGGER.trace("Indicated client name for service extraction is [{}]", clientName);

        if (StringUtils.isBlank(clientName)) {
            LOGGER.trace("No client name found in the request");
            return null;
        }

        val client = (BaseClient<Credentials, CommonProfile>) this.clients.findClient(clientName);
        val webContext = Pac4jUtils.getPac4jJ2EContext(request);
        val clientId = delegatedClientWebflowManager.getDelegatedClientId(webContext, client);
        if (StringUtils.isNotBlank(clientId)) {
            val ticket = delegatedClientWebflowManager.retrieveSessionTicketViaClientId(webContext, clientId);

            if (ticket == null || ticket.getService() == null) {
                LOGGER.warn("Session ticket [{}] is not found or does not have a service associated with it", ticket);
                return null;
            }
            return ticket.getService().getId();
        }
        return null;
    }
}

