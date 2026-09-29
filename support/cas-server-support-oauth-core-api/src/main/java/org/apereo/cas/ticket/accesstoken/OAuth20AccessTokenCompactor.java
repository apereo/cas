package org.apereo.cas.ticket.accesstoken;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.expiration.FixedInstantExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.util.DateTimeUtils;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

/**
 * This is {@link OAuth20AccessTokenCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@SuppressWarnings("EnumOrdinal")
public class OAuth20AccessTokenCompactor implements TicketCompactor<OAuth20AccessToken> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;

    @Getter
    private long maximumTicketLength = 384;
    
    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val accessToken = (OAuth20AccessToken) ticket;
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(accessToken.getService().getShortenedId()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(accessToken.getClientId()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValues(accessToken.getScopes()));
        builder.append(DELIMITER).append(accessToken.getResponseType() != null ? accessToken.getResponseType().ordinal() : OAuth20ResponseTypes.CODE.ordinal());
        builder.append(DELIMITER).append(accessToken.getGrantType() != null ? accessToken.getGrantType().ordinal() : OAuth20GrantTypes.AUTHORIZATION_CODE.ordinal());
        
        builder.append(compactAuthenticationAttempt(accessToken).toString());
        return builder.toString();
    }

    
    @Override
    public Class<OAuth20AccessToken> getTicketType() {
        return OAuth20AccessToken.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId, 8);
        val service = serviceFactory.createService(TicketCompactor.decodeValue(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex())));
        val clientId = TicketCompactor.decodeValue(structure.ticketElements().get(3));
        val scopes = TicketCompactor.decodeValues(structure.ticketElements().get(4));

        val responseType = OAuth20ResponseTypes.values()[Integer.parseInt(structure.ticketElements().get(5))];
        val grantType = OAuth20GrantTypes.values()[Integer.parseInt(structure.ticketElements().get(6))];

        val authentication = expandAuthentication(principalFactory, structure);
        val accessTokenFactory = (OAuth20AccessTokenFactory) ticketFactory.getObject().get(getTicketType());
        val accessToken = accessTokenFactory.create(service, authentication, scopes, clientId, responseType, grantType);
        accessToken.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        accessToken.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return accessToken;
    }

    protected Authentication expandAuthentication(final PrincipalFactory principalFactory, final CompactTicket structure) throws Throwable {
        val authenticationData = Splitter.on(":").splitToList(structure.ticketElements().get(7));
        Assert.isTrue(authenticationData.size() == 3, "Invalid compact authentication");
        val principal = principalFactory.createPrincipal(TicketCompactor.decodeValue(authenticationData.getFirst()));
        val handlers = new HashSet<>(TicketCompactor.decodeValues(authenticationData.get(1)));
        val credentialTypes = new HashSet<>(TicketCompactor.decodeValues(authenticationData.get(2)));

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

    protected StringBuilder compactAuthenticationAttempt(final OAuth20AccessToken code) {
        val authentication = code.getAuthentication();
        val builder = new StringBuilder();
        if (authentication != null) {
            val handlers = TicketCompactor.encodeValues(authentication.getSuccesses().keySet());
            val principalId = TicketCompactor.encodeValue(authentication.getPrincipal().getId());
            val credentialTypes = TicketCompactor.encodeValues(authentication.getCredentials().stream()
                .map(credential -> credential.getClass().getSimpleName()).toList());
            builder.append(DELIMITER).append(principalId).append(':').append(handlers).append(':').append(credentialTypes);
        }
        return builder;
    }
}
