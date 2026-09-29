package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

/**
 * This is {@link ProxyTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyTicketCompactor implements TicketCompactor<ProxyTicket> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;
    @Getter
    private long maximumTicketLength = 256;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val proxyTicket = (ProxyTicket) ticket;
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(proxyTicket.getService().getShortenedId()));
        builder.append(compactAuthenticationAttempt(proxyTicket).toString());
        return builder.toString();
    }

    @Override
    public Class<ProxyTicket> getTicketType() {
        return ProxyTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId, 5);
        val service = serviceFactory.createService(TicketCompactor.decodeValue(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex())));
        val authentication = expandAuthentication(principalFactory, structure);
        val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(ServiceTicket.class);
        val serviceTicket = serviceTicketFactory.create(service, authentication, false, ServiceTicket.class);

        val proxyTicketFactory = (ProxyTicketFactory) ticketFactory.getObject().get(getTicketType());
        val proxyGrantingTicketFactory = (ProxyGrantingTicketFactory) ticketFactory.getObject().get(ProxyGrantingTicket.class);
        val proxyGrantingTicket = proxyGrantingTicketFactory.create(serviceTicket, authentication);

        val proxyTicket = proxyTicketFactory.create(proxyGrantingTicket, service);
        proxyTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        proxyTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return proxyTicket;
    }

    protected StringBuilder compactAuthenticationAttempt(final AuthenticationAwareTicket authenticationAwareTicket) {
        val authentication = authenticationAwareTicket.getAuthentication();
        val builder = new StringBuilder();
        if (authentication != null) {
            Assert.isTrue(!authentication.getSuccesses().isEmpty(), "Authentication successes cannot be empty");
            val handlers = TicketCompactor.encodeValues(authentication.getSuccesses().keySet());
            val principalId = TicketCompactor.encodeValue(authentication.getPrincipal().getId());
            val credentialTypes = TicketCompactor.encodeValues(Optional.ofNullable(authentication.getAttributes().get(Credential.CREDENTIAL_TYPE_ATTRIBUTE))
                .stream()
                .flatMap(List::stream)
                .toList());
            builder.append(DELIMITER).append(principalId).append(':').append(handlers).append(':').append(credentialTypes);
            val rememberMe = BooleanUtils.toString(CoreAuthenticationUtils.isRememberMeAuthentication(authentication), "1", "0");
            builder.append(DELIMITER).append(rememberMe);
        }
        return builder;
    }

    protected Authentication expandAuthentication(final PrincipalFactory principalFactory, final CompactTicket structure) throws Throwable {
        val authenticationData = Splitter.on(":").splitToList(structure.ticketElements().get(3));
        Assert.isTrue(authenticationData.size() == 3, "Invalid compact authentication");
        val principal = principalFactory.createPrincipal(TicketCompactor.decodeValue(authenticationData.getFirst()));
        val handlers = new HashSet<>(TicketCompactor.decodeValues(authenticationData.get(1)));
        val credentialTypes = new HashSet<>(TicketCompactor.decodeValues(authenticationData.get(2)));
        Assert.isTrue(!handlers.isEmpty(), "Authentication handlers cannot be empty");
        return DefaultAuthenticationBuilder
            .newInstance()
            .setPrincipal(principal)
            .addAttribute(Credential.CREDENTIAL_TYPE_ATTRIBUTE, credentialTypes)
            .addAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, handlers)
            .setSuccesses(handlers.stream().collect(Collectors.toMap(Function.identity(),
                name -> new DefaultAuthenticationHandlerExecutionResult(name, principal))))
            .addAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, handlers)
            .build();
    }
}
