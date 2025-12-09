package org.apereo.cas.ticket.registry.compact;

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
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.EncodingUtils;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is {@link ProxyGrantingTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class ProxyGrantingTicketCompactor implements TicketCompactor<ProxyGrantingTicket> {
    private final ObjectProvider<@NonNull TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val proxyGrantingTicket = (ProxyGrantingTicket) ticket;
        builder.append(String.format("%s%s", DELIMITER, proxyGrantingTicket.getProxiedBy().getShortenedId()));
        builder.append(compactAuthenticationAttempt(proxyGrantingTicket).toString());
        return builder.toString();
    }

    @Override
    public Class<ProxyGrantingTicket> getTicketType() {
        return ProxyGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val proxyGrantingTicketFactory = (ProxyGrantingTicketFactory) ticketFactory.getObject().get(getTicketType());
        val authentication = expandAuthentication(principalFactory, structure);
        val serviceTicketFactory = (ServiceTicketFactory) ticketFactory.getObject().get(ServiceTicket.class);
        val service = serviceFactory.createService(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex()));
        val serviceTicket = serviceTicketFactory.create(service, authentication, false, ServiceTicket.class);
        
        val proxyGrantingTicket = proxyGrantingTicketFactory.create(serviceTicket, authentication);
        proxyGrantingTicket.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        proxyGrantingTicket.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));

        return proxyGrantingTicket;
    }

    protected Authentication expandAuthentication(final PrincipalFactory principalFactory, final CompactTicket structure) throws Throwable {
        val authenticationData = Splitter.on(":").splitToList(structure.ticketElements().get(3));
        val principalId = new String(EncodingUtils.decodeUrlSafeBase64(authenticationData.getFirst()), StandardCharsets.UTF_8);
        val principal = principalFactory.createPrincipal(principalId);
        val handlers = Arrays.stream(authenticationData.get(1).split("#")).collect(Collectors.toSet());
        val credentialTypes = Arrays.stream(authenticationData.get(2).split("#")).collect(Collectors.toSet());
        val rememberMe = BooleanUtils.toBoolean(structure.ticketElements().get(4));

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
    
    protected StringBuilder compactAuthenticationAttempt(final AuthenticationAwareTicket authenticationAwareTicket) {
        val authentication = authenticationAwareTicket.getAuthentication();
        val builder = new StringBuilder();
        if (authentication != null) {
            val handlers = String.join("#", authentication.getSuccesses().keySet());
            val principalId = EncodingUtils.encodeUrlSafeBase64(authentication.getPrincipal().getId());
            val credentialTypes = authentication.getCredentials().stream()
                .map(credential -> credential.getClass().getSimpleName()).collect(Collectors.joining("#"));
            builder.append(String.format("%s%s:%s:%s", DELIMITER, principalId, handlers, credentialTypes));
            builder.append(String.format("%s%s", DELIMITER, BooleanUtils.toString(CoreAuthenticationUtils.isRememberMeAuthentication(authentication), "1", "0")));
        }
        return builder;
    }
}
