package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.RememberMeCredential;
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
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link ServiceTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ServiceTicketCompactor implements TicketCompactor<ServiceTicket> {
    private final ObjectProvider<@NonNull TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;
    @Getter
    private long maximumTicketLength = 256;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        if (ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService())) {
            builder.append(String.format("%s%s", DELIMITER, StringUtils.defaultString(sat.getService().getShortenedId())));
        } else {
            builder.append("%s*".formatted(DELIMITER));
        }
        if (ticket instanceof final RenewableServiceTicket rst) {
            builder.append(String.format("%s%s", DELIMITER, BooleanUtils.toString(rst.isFromNewLogin(), "1", "0")));
        } else {
            builder.append("%s0".formatted(DELIMITER));
        }

        if (ticket instanceof final AuthenticationAwareTicket aat) {
            builder.append(compactAuthenticationAttempt(aat).toString());
        } else {
            builder.append("%s*%s0".formatted(DELIMITER, DELIMITER));
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

        val service = serviceFactory.createService(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex()));
        val credentialsProvided = BooleanUtils.toBoolean(structure.ticketElements().get(3));
        val authentication = expandAuthentication(principalFactory, structure);
        val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(getTicketType());
        val serviceTicket = serviceTicketFactory.create(service, authentication, credentialsProvided, getTicketType());
        serviceTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        serviceTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return serviceTicket;
    }

    protected StringBuilder compactAuthenticationAttempt(final AuthenticationAwareTicket authenticationAwareTicket) {
        val authentication = authenticationAwareTicket.getAuthentication();
        val builder = new StringBuilder();
        if (authentication != null) {
            val handlers = String.join("#", authentication.getSuccesses().keySet());
            val principalId = authentication.getPrincipal().getId();
            val credentialTypes = authentication.getCredentials().stream()
                .map(credential -> credential.getClass().getSimpleName()).collect(Collectors.joining("#"));
            builder.append(String.format("%s%s:%s:%s", DELIMITER, principalId, handlers, credentialTypes));

            val rememberMe = BooleanUtils.toString(CoreAuthenticationUtils.isRememberMeAuthentication(authentication), "1", "0");
            builder.append(String.format("%s%s", DELIMITER, rememberMe));
        }
        return builder;
    }

    protected Authentication expandAuthentication(final PrincipalFactory principalFactory, final CompactTicket structure) throws Throwable {
        val authenticationData = Splitter.on(":").splitToList(structure.ticketElements().get(4));
        val principal = principalFactory.createPrincipal(authenticationData.getFirst());
        val handlers = Arrays.stream(authenticationData.get(1).split("#")).collect(Collectors.toSet());
        val credentialTypes = Arrays.stream(authenticationData.get(2).split("#")).collect(Collectors.toSet());
        val rememberMe = BooleanUtils.toBoolean(structure.ticketElements().get(5));

        return DefaultAuthenticationBuilder
            .newInstance()
            .setPrincipal(principal)
            .addAttribute(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, rememberMe)
            .addAttribute(Credential.CREDENTIAL_TYPE_ATTRIBUTE, credentialTypes)
            .addAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, handlers)
            .setSuccesses(handlers.stream().collect(Collectors.toMap(Function.identity(),
                name -> new DefaultAuthenticationHandlerExecutionResult(name, principal))))
            .addAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, handlers)
            .build();
    }
}
