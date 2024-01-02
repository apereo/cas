package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.RenewableServiceTicket;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link org.apereo.cas.ticket.registry.ServiceTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ServiceTicketCompactor implements TicketCompactor<ServiceTicket> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) {
        if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
            builder.append(String.format("%s%s", DELIMITER, StringUtils.defaultString(sat.getService().getId())));
        } else {
            builder.append("%s*".formatted(DELIMITER));
        }
        if (ticket instanceof final RenewableServiceTicket rst) {
            builder.append(String.format("%s%s", DELIMITER, BooleanUtils.toString(rst.isFromNewLogin(), "1", "0")));
        } else {
            builder.append("%s0".formatted(DELIMITER));
        }
        if (ticket instanceof final AuthenticationAwareTicket aat && aat.getAuthentication() != null) {
            val handlers = String.join("#", aat.getAuthentication().getSuccesses().keySet());
            val principalId = aat.getAuthentication().getPrincipal().getId();
            builder.append(String.format("%s%s:%s", DELIMITER, principalId, handlers));
        } else {
            builder.append("%s*".formatted(DELIMITER));
        }
        return builder.toString();
    }

    @Override
    public Class<ServiceTicket> getTicketType() {
        return ServiceTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(getTicketType());

        val service = serviceFactory.createService(structure.ticketElements().get(2));
        val credentialsProvided = BooleanUtils.toBoolean(structure.ticketElements().get(3));

        val principalAndHandlers = structure.ticketElements().get(4);
        val principal = principalFactory.createPrincipal(StringUtils.substringBefore(principalAndHandlers, ":"));
        val handlers = Arrays.stream(StringUtils.substringAfter(principalAndHandlers, ":")
            .split("#")).collect(Collectors.toSet());

        val authentication = DefaultAuthenticationBuilder
            .newInstance()
            .setPrincipal(principal)
            .addAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, handlers)
            .setSuccesses(handlers.stream().collect(Collectors.toMap(Function.identity(),
                name -> new DefaultAuthenticationHandlerExecutionResult(name, principal))))
            .build();
        val serviceTicket = serviceTicketFactory.create(service, authentication, credentialsProvided, getTicketType());
        serviceTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return serviceTicket;
    }
}
