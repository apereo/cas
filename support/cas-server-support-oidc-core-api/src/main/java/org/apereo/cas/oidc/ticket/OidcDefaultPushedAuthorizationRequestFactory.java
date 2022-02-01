package org.apereo.cas.oidc.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcDefaultPushedAuthorizationRequestFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class OidcDefaultPushedAuthorizationRequestFactory implements OidcPushedAuthorizationRequestFactory {
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
        return OidcPushedAuthorizationRequest.class;
    }

    @Override
    public OidcPushedAuthorizationRequest create(final AccessTokenRequestDataHolder holder) throws Exception {
        val request = MAPPER.writeValueAsString(holder);
        val id = idGenerator.getNewTicketId(OidcPushedAuthorizationRequest.PREFIX);
        val expirationPolicy = determineExpirationPolicyForService(holder.getRegisteredService());
        return new OidcDefaultPushedAuthorizationRequest(id, expirationPolicy,
            holder.getAuthentication(), holder.getService(), holder.getRegisteredService(),
            EncodingUtils.encodeBase64(request));
    }

    @Override
    public AccessTokenRequestDataHolder toAccessTokenRequest(final OidcPushedAuthorizationRequest authzRequest) throws Exception {
        val decodedRequest = EncodingUtils.decodeBase64ToString(authzRequest.getAuthorizationRequest());
        return MAPPER.readValue(decodedRequest, AccessTokenRequestDataHolder.class);
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
