package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Default OAuth code factory.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OAuth20DefaultOAuthCodeFactory implements OAuth20CodeFactory {

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator oAuthCodeIdGenerator;

    /**
     * ExpirationPolicy for tokens.
     */
    protected final ExpirationPolicyBuilder<OAuth20Code> expirationPolicy;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    public OAuth20DefaultOAuthCodeFactory(final ExpirationPolicyBuilder<OAuth20Code> expirationPolicy, final ServicesManager servicesManager) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy, servicesManager);
    }

    @Override
    public OAuth20Code create(final Service service,
                              final Authentication authentication,
                              final TicketGrantingTicket ticketGrantingTicket,
                              final Collection<String> scopes,
                              final String codeChallenge,
                              final String codeChallengeMethod,
                              final String clientId,
                              final Map<String, Map<String, Object>> requestClaims) {

        val expirationPolicyToUse = determineExpirationPolicyForService(clientId);
        val codeId = this.oAuthCodeIdGenerator.getNewTicketId(OAuth20Code.PREFIX);
        return new OAuth20DefaultCode(codeId, service, authentication,
            expirationPolicyToUse, ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod, clientId, requestClaims);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final String clientId) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        if (registeredService != null && registeredService.getCodeExpirationPolicy() != null) {
            val policy = registeredService.getCodeExpirationPolicy();
            val count = policy.getNumberOfUses();
            val ttl = policy.getTimeToLive();
            if (count > 0 && StringUtils.isNotBlank(ttl)) {
                return new OAuth20CodeExpirationPolicy(count, Beans.newDuration(ttl).getSeconds());
            }
        }
        return this.expirationPolicy.buildTicketExpirationPolicy();
    }
}
