package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.CibaRequestContext;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.CompactTicketCodec;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OidcCibaRequestCompactor}. It keeps the client id, scopes, resources, principal id, client notification
 * token, user code, binding message and tenant of the request, and builds the request again through the CIBA request
 * factory. The principal keeps its id and gets its attributes from the principal resolver again, as the ticket-granting
 * ticket does. The ready flag is not kept, so only the push delivery mode works with the stateless ticket registry.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcCibaRequestCompactor implements TicketCompactor<OidcCibaRequest> {
    private static final int CLIENT_ID_INDEX = 2;

    private static final int SCOPES_INDEX = 3;

    private static final int RESOURCES_INDEX = 4;

    private static final int PRINCIPAL_INDEX = 5;

    private static final int CLIENT_NOTIFICATION_TOKEN_INDEX = 6;

    private static final int USER_CODE_INDEX = 7;

    private static final int BINDING_MESSAGE_INDEX = 8;

    private static final int TENANT_INDEX = 9;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final PrincipalFactory principalFactory;

    private final ObjectProvider<PrincipalResolver> principalResolver;

    @Getter
    private long maximumTicketLength = 1024;

    @Override
    public Class<OidcCibaRequest> getTicketType() {
        return OidcCibaRequest.class;
    }

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        val cibaRequest = (OidcCibaRequest) ticket;
        val authentication = cibaRequest.getAuthentication();
        fields.add(StringUtils.defaultString(cibaRequest.getClientId()));
        fields.add(CompactTicketCodec.encodeValues(cibaRequest.getScopes()));
        fields.add(CompactTicketCodec.encodeValues(cibaRequest.getResources()));
        fields.add(authentication.getPrincipal().getId());
        fields.add(StringUtils.defaultString(authentication.getSingleValuedAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN, String.class)));
        fields.add(StringUtils.defaultString(authentication.getSingleValuedAttribute(OidcConstants.USER_CODE, String.class)));
        fields.add(StringUtils.defaultString(authentication.getSingleValuedAttribute(OidcConstants.BINDING_MESSAGE, String.class)));
        fields.add(StringUtils.defaultString(cibaRequest.getTenantId()));
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, TENANT_INDEX + 1);
        val principal = resolvePrincipal(structure.get(PRINCIPAL_INDEX));
        val context = CibaRequestContext.builder()
            .clientId(structure.get(CLIENT_ID_INDEX))
            .scope(new LinkedHashSet<>(CompactTicketCodec.decodeValues(structure.get(SCOPES_INDEX))))
            .resources(new LinkedHashSet<>(CompactTicketCodec.decodeValues(structure.get(RESOURCES_INDEX))))
            .principal(principal)
            .clientNotificationToken(StringUtils.trimToNull(structure.get(CLIENT_NOTIFICATION_TOKEN_INDEX)))
            .userCode(StringUtils.trimToNull(structure.get(USER_CODE_INDEX)))
            .bindingMessage(StringUtils.trimToNull(structure.get(BINDING_MESSAGE_INDEX)))
            .tenant(StringUtils.trimToNull(structure.get(TENANT_INDEX)))
            .build();
        val factory = (OidcCibaRequestFactory) ticketFactory.getObject().get(getTicketType());
        val cibaRequest = factory.create(context);
        cibaRequest.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        cibaRequest.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return cibaRequest;
    }

    private Principal resolvePrincipal(final String principalId) throws Throwable {
        val principal = Objects.requireNonNull(principalFactory.createPrincipal(principalId));
        val resolved = principalResolver.getObject().resolve(new BasicIdentifiableCredential(principalId),
            Optional.of(principal), Optional.empty(), Optional.empty());
        return resolved == null ? principal : principal.withAttributes(new HashMap<>(resolved.getAttributes()));
    }
}
