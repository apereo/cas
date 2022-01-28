package org.apereo.cas.oidc.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcDefaultPushedAuthorizationUriFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class OidcDefaultPushedAuthorizationUriFactory implements OidcPushedAuthorizationUriFactory {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator idGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicyBuilder;

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OidcPushedAuthorizationUri.class;
    }

    @Override
    public OidcPushedAuthorizationUri create(final AccessTokenRequestDataHolder holder) throws Exception {
        val request = MAPPER.writeValueAsString(holder);
        val id = idGenerator.getNewTicketId(OidcPushedAuthorizationUri.PREFIX);
        val expirationPolicy = determineExpirationPolicyForService(holder.getRegisteredService());
        return new OidcDefaultPushedAuthorizationUri(id, expirationPolicy, request);
    }

    /**
     * Determine the expiration policy for the registered service.
     *
     * @param registeredService the registered service
     * @return the expiration policy
     */
    protected ExpirationPolicy determineExpirationPolicyForService(final OAuthRegisteredService registeredService) {
        return this.expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
