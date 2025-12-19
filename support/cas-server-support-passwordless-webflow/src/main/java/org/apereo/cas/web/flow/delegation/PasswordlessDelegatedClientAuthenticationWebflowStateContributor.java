package org.apereo.cas.web.flow.delegation;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowStateContributor;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

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
                                                     final WebContext webContext,
                                                     final Client client) {
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        val passwordlessRequest = PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class);
        return Optional.ofNullable(account)
            .map(acct -> Map.of(PasswordlessUserAccount.class.getName(), account,
                PasswordlessAuthenticationRequest.class.getName(), passwordlessRequest))
            .orElseGet(Map::of);
    }

    @Override
    public @Nullable Service restore(final RequestContext requestContext, final WebContext webContext,
                                     final Optional<TransientSessionTicket> givenTicket, final Client client) {
        return givenTicket
            .map(ticket -> {
                val account = ticket.getProperty(PasswordlessUserAccount.class.getName(), PasswordlessUserAccount.class);
                PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, account);

                val passwordlessRequest = ticket.getProperty(PasswordlessAuthenticationRequest.class.getName(), PasswordlessAuthenticationRequest.class);
                PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
                
                return ticket.getService();
            })
            .orElse(null);

    }
}
