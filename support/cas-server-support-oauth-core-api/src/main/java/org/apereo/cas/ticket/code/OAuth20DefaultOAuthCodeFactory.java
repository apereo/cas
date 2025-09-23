package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OAuth20DefaultOAuthCodeFactory implements OAuth20CodeFactory {

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator ticketIdGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<OAuth20Code> expirationPolicyBuilder;

    /**
     * Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Cipher to sign/encrypt codes, if enabled.
     */
    protected final CipherExecutor<String, String> cipherExecutor;

    protected final TicketTrackingPolicy descendantTicketsTrackingPolicy;

    @Override
    public OAuth20Code create(final Service service,
                              final Authentication authentication,
                              final Ticket ticketGrantingTicket,
                              final Collection<String> scopes,
                              final String codeChallenge,
                              final String codeChallengeMethod,
                              final String clientId,
                              final Map<String, Map<String, Object>> requestClaims,
                              final OAuth20ResponseTypes responseType,
                              final OAuth20GrantTypes grantType) throws Throwable {

        val expirationPolicyToUse = determineExpirationPolicyForService(clientId);
        val codeId = ticketIdGenerator.getNewTicketId(OAuth20Code.PREFIX);

        val codeIdToUse = FunctionUtils.doIf(cipherExecutor.isEnabled(), () -> {
            LOGGER.trace("Attempting to encode OAuth code [{}]", codeId);
            val encoded = cipherExecutor.encode(codeId);
            LOGGER.debug("Encoded OAuth code [{}]", encoded);
            return encoded;
        }, () -> codeId).get();

        val oauthCode = new OAuth20DefaultCode(codeIdToUse, service, authentication,
            expirationPolicyToUse, ticketGrantingTicket, scopes,
            codeChallenge, codeChallengeMethod, clientId,
            requestClaims, responseType, grantType);
        FunctionUtils.doIfNotNull(service, __ -> oauthCode.setTenantId(service.getTenant()));
        descendantTicketsTrackingPolicy.trackTicket(ticketGrantingTicket, oauthCode);
        return oauthCode;
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OAuth20Code.class;
    }

    private ExpirationPolicy determineExpirationPolicyForService(final String clientId) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        if (registeredService != null && registeredService.getCodeExpirationPolicy() != null) {
            val policy = registeredService.getCodeExpirationPolicy();
            val count = policy.getNumberOfUses();
            val ttl = policy.getTimeToLive();
            if (count > 0 && StringUtils.isNotBlank(ttl)) {
                return new OAuth20CodeExpirationPolicy(count, Beans.newDuration(ttl).toSeconds());
            }
        }
        return this.expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
