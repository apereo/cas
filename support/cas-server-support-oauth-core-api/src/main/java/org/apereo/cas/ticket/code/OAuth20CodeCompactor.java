package org.apereo.cas.ticket.code;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.CompactTicketAuthentication;
import org.apereo.cas.ticket.registry.CompactTicketCodec;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OAuth20CodeCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@SuppressWarnings("EnumOrdinal")
public class OAuth20CodeCompactor implements TicketCompactor<OAuth20Code> {
    private static final int CLIENT_ID_INDEX = 3;

    private static final int SCOPES_INDEX = 4;

    private static final int CODE_CHALLENGE_INDEX = 5;

    private static final int CODE_CHALLENGE_METHOD_INDEX = 6;

    private static final int RESPONSE_TYPE_INDEX = 7;

    private static final int GRANT_TYPE_INDEX = 8;

    private static final int AUTHENTICATION_INDEX = 9;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    @Getter
    private long maximumTicketLength = 384;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) throws Exception {
        val code = (OAuth20Code) ticket;
        fields.add(StringUtils.defaultString(code.getService().getShortenedId()));
        fields.add(StringUtils.defaultString(code.getClientId()));
        fields.add(CompactTicketCodec.encodeValues(code.getScopes()));
        fields.add(StringUtils.defaultString(code.getCodeChallenge()));
        fields.add(StringUtils.defaultString(code.getCodeChallengeMethod()));
        fields.add(Objects.requireNonNullElse(code.getResponseType(), OAuth20ResponseTypes.CODE).name());
        fields.add(Objects.requireNonNullElse(code.getGrantType(), OAuth20GrantTypes.AUTHORIZATION_CODE).name());
        CompactTicketAuthentication.compact(fields, code.getAuthentication());
    }

    @Override
    public Class<OAuth20Code> getTicketType() {
        return OAuth20Code.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, AUTHENTICATION_INDEX + CompactTicketAuthentication.FIELD_COUNT);
        val service = Objects.requireNonNull(serviceFactory.createService(structure.get(CompactTicketIndexes.SERVICE)));
        val authentication = CompactTicketAuthentication.expand(principalFactory, structure.ticketElements(), AUTHENTICATION_INDEX);
        val responseType = OAuth20ResponseTypes.valueOf(structure.get(RESPONSE_TYPE_INDEX));
        val grantType = OAuth20GrantTypes.valueOf(structure.get(GRANT_TYPE_INDEX));
        val code = new OAuth20DefaultCode(OAuth20Code.PREFIX, service, authentication,
            new FixedInstantExpirationPolicy(structure.expirationTime()), null,
            CompactTicketCodec.decodeValues(structure.get(SCOPES_INDEX)),
            StringUtils.trimToNull(structure.get(CODE_CHALLENGE_INDEX)),
            StringUtils.trimToNull(structure.get(CODE_CHALLENGE_METHOD_INDEX)),
            structure.get(CLIENT_ID_INDEX), new HashMap<>(), responseType, grantType);
        code.setTenantId(service.getTenant());
        code.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return code;
    }
}
