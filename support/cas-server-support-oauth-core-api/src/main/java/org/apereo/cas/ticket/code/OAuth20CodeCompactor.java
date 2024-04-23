package org.apereo.cas.ticket.code;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        builder.append(String.format("%s%s", DELIMITER, code.getService().getShortenedId()));
        builder.append(String.format("%s%s", DELIMITER, code.getClientId()));
        builder.append(String.format("%s%s", DELIMITER, String.join("|", code.getScopes())));
        builder.append(String.format("%s%s", DELIMITER, StringUtils.defaultString(code.getCodeChallenge())));
        builder.append(String.format("%s%s", DELIMITER, StringUtils.defaultString(code.getCodeChallengeMethod())));
        builder.append(String.format("%s%s", DELIMITER, code.getResponseType() != null ? code.getResponseType().ordinal() : OAuth20ResponseTypes.CODE.ordinal()));
        builder.append(String.format("%s%s", DELIMITER, code.getGrantType() != null ? code.getGrantType().ordinal() : OAuth20GrantTypes.AUTHORIZATION_CODE.ordinal()));
        builder.append(compactAuthenticationAttempt(code).toString());
        return builder.toString();
    }

    @Override
    public Class<OAuth20Code> getTicketType() {
        return OAuth20Code.class;
    }

    @Override
    public Ticket expand(final String ticketId) throws Throwable {
        val structure = parse(ticketId);
        val service = serviceFactory.createService(structure.ticketElements().get(CompactTicketIndexes.SERVICE.getIndex()));
        val clientId = structure.ticketElements().get(3);
        val scopes = StringUtils.isNotBlank(structure.ticketElements().get(4))
            ? Splitter.on("|").splitToList(structure.ticketElements().get(4))
            : new HashSet<String>();
        val codeChallenge = StringUtils.trimToNull(structure.ticketElements().get(5));
        val codeChallengeMethod = StringUtils.trimToNull(structure.ticketElements().get(6));

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
        val principal = principalFactory.createPrincipal(authenticationData.getFirst());
        val handlers = Arrays.stream(authenticationData.get(1).split("#")).collect(Collectors.toSet());
        val credentialTypes = Arrays.stream(authenticationData.get(2).split("#")).collect(Collectors.toSet());

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
            val handlers = String.join("#", authentication.getSuccesses().keySet());
            val principalId = authentication.getPrincipal().getId();
            val credentialTypes = authentication.getCredentials().stream()
                .map(credential -> credential.getClass().getSimpleName()).collect(Collectors.joining("#"));
            builder.append(String.format("%s%s:%s:%s", DELIMITER, principalId, handlers, credentialTypes));
        }
        return builder;
    }
}
