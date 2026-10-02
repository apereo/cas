package org.apereo.cas.mfa.simple.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketCompactor}.
 * Keeps the service, the code sent to the user and the principal id. The expanded ticket is created by the
 * ticket factory with the code as its id; the registry then gives it the id it was looked up by, so the code
 * is also kept under {@link CasSimpleMultifactorAuthenticationTicket#PROPERTY_CODE}. The principal is rebuilt
 * from its id; attributes are resolved again when the token is validated.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasSimpleMultifactorAuthenticationTicketCompactor implements TicketCompactor<CasSimpleMultifactorAuthenticationTicket> {
    private static final int CODE_INDEX = 3;

    private static final int PRINCIPAL_INDEX = 4;

    private final ObjectProvider<TicketFactory> ticketFactory;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        val token = (CasSimpleMultifactorAuthenticationTicket) ticket;
        val service = token.getService();
        fields.add(service != null ? StringUtils.defaultString(service.getId()) : StringUtils.EMPTY);
        fields.add(CasSimpleMultifactorAuthenticationTicket.getCode(token));
        val principal = token.getProperties().get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL);
        fields.add(principal instanceof final Principal tokenPrincipal ? tokenPrincipal.getId() : StringUtils.EMPTY);
    }

    @Override
    public Class<CasSimpleMultifactorAuthenticationTicket> getTicketType() {
        return CasSimpleMultifactorAuthenticationTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) throws Throwable {
        val structure = parse(compactTicket, PRINCIPAL_INDEX + 1);
        val url = structure.get(CompactTicketIndexes.SERVICE);
        val service = StringUtils.isNotBlank(url) ? serviceFactory.createService(url) : null;
        val code = structure.get(CODE_INDEX);
        val properties = new HashMap<String, Serializable>();
        properties.put(CasSimpleMultifactorAuthenticationTicket.PROPERTY_CODE, code);
        val principalId = structure.get(PRINCIPAL_INDEX);
        if (StringUtils.isNotBlank(principalId)) {
            properties.put(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL, principalFactory.createPrincipal(principalId));
        }
        val factory = (CasSimpleMultifactorAuthenticationTicketFactory) ticketFactory.getObject().get(getTicketType());
        val token = factory.create(code, service, properties);
        token.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        token.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        return token;
    }
}
