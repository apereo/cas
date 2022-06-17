package org.apereo.cas.web.flow.delegation;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link PasswordlessDelegatedClientAuthenticationWebflowStateContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class PasswordlessDelegatedClientAuthenticationWebflowStateContributor
    implements DelegatedClientAuthenticationWebflowStateContributor {

    @Override
    public Map<String, ? extends Serializable> store(final RequestContext requestContext,
                                                     final WebContext webContext, final Client client) {
        val account = WebUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        return Optional.ofNullable(account)
            .map(acct -> Map.of(PasswordlessUserAccount.class.getName(), account))
            .orElseGet(Map::of);
    }

    @Override
    public Service restore(final RequestContext requestContext, final WebContext webContext,
                           final TransientSessionTicket ticket, final Client client) {
        val account = ticket.getProperty(PasswordlessUserAccount.class.getName(), PasswordlessUserAccount.class);
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, account);
        return ticket.getService();
    }
}
