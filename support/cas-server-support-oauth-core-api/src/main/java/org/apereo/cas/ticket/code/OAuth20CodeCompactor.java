package org.apereo.cas.ticket.code;

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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

/**
 * This is {@link OAuth20CodeCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@SuppressWarnings("EnumOrdinal")
public class OAuth20CodeCompactor implements TicketCompactor<OAuth20Code> {
    private final ObjectProvider<TicketFactory> ticketFactory;
    private final ServiceFactory serviceFactory;
    private final PrincipalFactory principalFactory;
    @Getter
    private long maximumTicketLength = 384;

    @Override
    public String compact(final StringBuilder builder, final Ticket ticket) throws Exception {
        val code = (OAuth20Code) ticket;
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getService().getShortenedId()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getClientId()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValues(code.getScopes()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getCodeChallenge()));
        builder.append(DELIMITER).append(TicketCompactor.encodeValue(code.getCodeChallengeMethod()));
        builder.append(DELIMITER).append(code.getResponseType() != null ? code.getResponseType().ordinal() : OAuth20ResponseTypes.CODE.ordinal());
        builder.append(DELIMITER).append(code.getGrantType() != null ? code.getGrantType().ordinal() : OAuth20GrantTypes.AUTHORIZATION_CODE.ordinal());
        builder.append(compactAuthenticationAttempt(code).toString());
        return builder.toString();
    }

    @Override
    public Class<OAuth20Code> getTicketType() {
        return OAuth20Code.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId, 10);
        val service = serviceFactory.createService(TicketCompactor.decodeValue(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex())));
        val clientId = TicketCompactor.decodeValue(structure.ticketElements().get(3));
        val scopes = TicketCompactor.decodeValues(structure.ticketElements().get(4));
        val codeChallenge = StringUtils.trimToNull(TicketCompactor.decodeValue(structure.ticketElements().get(5)));
        val codeChallengeMethod = StringUtils.trimToNull(TicketCompactor.decodeValue(structure.ticketElements().get(6)));

        val responseType = OAuth20ResponseTypes.values()[Integer.parseInt(structure.ticketElements().get(7))];
        val grantType = OAuth20GrantTypes.values()[Integer.parseInt(structure.ticketElements().get(8))];

        val authentication = expandAuthentication(principalFactory, structure);
        val codeFactory = (OAuth20CodeFactory) ticketFactory.getObject().get(getTicketType());
        val code = codeFactory.create(service, authentication, null,
            scopes, codeChallenge, codeChallengeMethod, clientId, new HashMap<>(), responseType, grantType);
        code.setExpirationPolicy(new FixedInstantExpirationPolicy(structure.expirationTime()));
        code.setCreationTime(DateTimeUtils.zonedDateTimeOf(structure.creationTime()));
        return code;
    }

    protected Authentication expandAuthentication(final PrincipalFactory principalFactory, final CompactTicket structure) throws Throwable {
        val authenticationData = Splitter.on(":").splitToList(structure.ticketElements().get(9));
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

    protected StringBuilder compactAuthenticationAttempt(final OAuth20Code code) {
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
