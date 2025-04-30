package org.apereo.cas.oidc.ticket;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * This is {@link OidcDefaultCibaRequestFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OidcDefaultCibaRequestFactory implements OidcCibaRequestFactory {
    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator idGenerator;

    @Getter
    protected final ExpirationPolicyBuilder<OidcCibaRequest> expirationPolicyBuilder;

    private final CipherExecutor<byte[], byte[]> cipherExecutor;
    
    @Override
    public Class<? extends Ticket> getTicketType() {
        return OidcCibaRequest.class;
    }

    @Override
    public OidcCibaRequest create(final CibaRequestContext holder) throws Throwable {
        val id = idGenerator.getNewTicketId(OidcCibaRequest.PREFIX);
        val expirationPolicy = holder.getRequestedExpiry() > 0
            ? new HardTimeoutExpirationPolicy(holder.getRequestedExpiry())
            : expirationPolicyBuilder.buildTicketExpirationPolicy();

        val authenticationBuilder = new DefaultAuthenticationBuilder(holder.getPrincipal())
            .addAttribute(OAuth20Constants.SCOPE, holder.getScope())
            .addAttribute(OAuth20Constants.CLIENT_ID, holder.getClientId());
        
        FunctionUtils.doIfNotBlank(holder.getUserCode(),
            userCode -> authenticationBuilder.addAttribute(OidcConstants.USER_CODE, userCode));
        FunctionUtils.doIfNotBlank(holder.getClientNotificationToken(),
            token -> authenticationBuilder.addAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN, token));
        FunctionUtils.doIfNotBlank(holder.getBindingMessage(),
            bindingMessage -> authenticationBuilder.addAttribute(OidcConstants.BINDING_MESSAGE, bindingMessage));
        
        val authentication = authenticationBuilder.build();
        val request = new OidcDefaultCibaRequest(id, authentication, expirationPolicy,
            holder.getScope(), holder.getClientId(), encodeCibaRequestId(id));
        request.setTenantId(holder.getTenant());
        return request;
    }

    @Override
    public String decodeId(final String requestId) {
        val decoded = EncodingUtils.decodeUrlSafeBase64(requestId);
        return new String((byte[]) cipherExecutor.withSigningDisabled().decode(decoded), StandardCharsets.UTF_8);
    }
    
    private String encodeCibaRequestId(final String id) {
        val encodedId = (byte[]) cipherExecutor.withSigningDisabled().encode(id.getBytes(StandardCharsets.UTF_8));
        return Objects.requireNonNull(EncodingUtils.encodeUrlSafeBase64(encodedId));
    }

}
