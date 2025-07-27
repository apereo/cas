package org.apereo.cas.oidc.ticket;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.Getter;
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
    /**
     * Default instance for the ticket id generator.
     */
    @Getter
    protected final UniqueTicketIdGenerator ticketIdGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicyBuilder;

    @Override
    public Class<? extends Ticket> getTicketType() {
        return OidcPushedAuthorizationRequest.class;
    }

    @Override
    public OidcPushedAuthorizationRequest create(final AccessTokenRequestContext holder) throws Throwable {
        val request = SerializationUtils.serialize(holder);
        val id = ticketIdGenerator.getNewTicketId(OidcPushedAuthorizationRequest.PREFIX);
        val expirationPolicy = determineExpirationPolicyForService(holder.getRegisteredService());
        val par = new OidcDefaultPushedAuthorizationRequest(id, expirationPolicy,
            holder.getAuthentication(), holder.getService(), holder.getRegisteredService(),
            EncodingUtils.encodeBase64(request));
        par.setTenantId(holder.getService().getTenant());
        FunctionUtils.doIfNotNull(holder.getService(), __ -> par.setTenantId(holder.getService().getTenant()));
        return par;
    }

    @Override
    public AccessTokenRequestContext toAccessTokenRequest(final OidcPushedAuthorizationRequest authzRequest) {
        val decodedRequest = EncodingUtils.decodeBase64(authzRequest.getAuthorizationRequest());
        return SerializationUtils.decodeAndDeserializeObject(decodedRequest,
            CipherExecutor.noOp(), AccessTokenRequestContext.class);
    }

    protected ExpirationPolicy determineExpirationPolicyForService(final OAuthRegisteredService registeredService) {
        return this.expirationPolicyBuilder.buildTicketExpirationPolicy();
    }
}
