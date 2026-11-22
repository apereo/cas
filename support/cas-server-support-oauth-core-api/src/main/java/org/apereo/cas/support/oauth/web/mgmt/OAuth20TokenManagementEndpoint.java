package org.apereo.cas.support.oauth.web.mgmt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryQueryCriteria;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.transaction.support.TransactionOperations;

/**
 * This is {@link OAuth20TokenManagementEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "oauthTokens", defaultAccess = Access.NONE)
@Slf4j
public class OAuth20TokenManagementEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<TicketRegistry> ticketRegistry;

    private final ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    private final TransactionOperations ticketTransactionOperations;

    public OAuth20TokenManagementEndpoint(final CasConfigurationProperties casProperties,
                                          final ObjectProvider<TicketRegistry> ticketRegistry,
                                          final ObjectProvider<JwtBuilder> accessTokenJwtBuilder,
                                          final TransactionOperations ticketTransactionOperations) {
        super(casProperties);
        this.ticketRegistry = ticketRegistry;
        this.accessTokenJwtBuilder = accessTokenJwtBuilder;
        this.ticketTransactionOperations = ticketTransactionOperations;
    }

    /**
     * Gets access and refresh tokens, all of them or only those issued to a user. The tokens of a user are asked of the
     * ticket registry by type and user, so registries that look tickets up by user, such as those backed by a database,
     * read only that user's tokens; how the user is matched, such as whether case matters, is up to the registry. All
     * tokens are found by scanning every ticket. Both run inside a ticket transaction, so that a registry backed by a
     * database reads the rows a batch at a time instead of collecting every ticket in memory first.
     *
     * @param username the user whose tokens to get; all tokens when blank
     * @return the access and refresh tokens
     */
    @ReadOperation
    @Operation(summary = "Get access and/or refresh tokens, optionally only those issued to a user",
        parameters = @Parameter(name = "username", required = false, description = "Only the tokens issued to this user"))
    public Collection<Ticket> getTokens(@Nullable final String username) {
        val user = StringUtils.trimToNull(username);
        if (user != null) {
            return getTokensIssuedTo(user);
        }
        val tokens = ticketTransactionOperations.execute(_ -> {
            try (val found = ticketRegistry
                .getObject()
                .getTickets(ticket -> isToken(ticket) && !ticket.isExpired())) {
                return found
                    .sorted(Comparator.comparing(Ticket::getId))
                    .<Ticket>map(Ticket.class::cast)
                    .collect(Collectors.toList());
            }
        });
        return Objects.requireNonNullElseGet(tokens, List::of);
    }

    /**
     * Gets access token.
     *
     * @param token the token id
     * @return the access token
     */
    @ReadOperation
    @Operation(summary = "Get single token by id", parameters = @Parameter(name = "token", required = true, description = "The token id"))
    public @Nullable Ticket getToken(@Selector final String token) {
        return FunctionUtils.doAndHandle(() -> {
            val ticketId = extractAccessTokenFrom(token);
            return ticketRegistry.getObject().getTicket(Objects.requireNonNull(ticketId), OAuth20Token.class);
        });
    }

    /**
     * Delete access token.
     *
     * @param token the ticket id
     * @throws Exception the exception
     */
    @DeleteOperation
    @Operation(summary = "Delete token by id", parameters = @Parameter(name = "token", required = true, description = "The token id"))
    public void deleteToken(@Selector final String token) throws Exception {
        val ticket = getToken(token);
        if (ticket instanceof OAuth20Token) {
            ticketRegistry.getObject().deleteTicket(ticket.getId());
        }
    }

    private Collection<Ticket> getTokensIssuedTo(final String username) {
        val registry = ticketRegistry.getObject();
        val tokens = ticketTransactionOperations.execute(_ -> Stream.of(OAuth20AccessToken.PREFIX, OAuth20RefreshToken.PREFIX)
            .map(prefix -> new TicketRegistryQueryCriteria().setType(prefix).setPrincipal(username).setDecode(true))
            .flatMap(criteria -> registry.query(criteria).stream())
            .filter(Ticket.class::isInstance)
            .map(Ticket.class::cast)
            .filter(ticket -> isToken(ticket) && !ticket.isExpired() && issuedTo(ticket, username))
            .sorted(Comparator.comparing(Ticket::getId))
            .collect(Collectors.toList()));
        return Objects.requireNonNullElseGet(tokens, List::of);
    }

    private static boolean isToken(final Ticket ticket) {
        return ticket instanceof OAuth20AccessToken || ticket instanceof OAuth20RefreshToken;
    }

    private static boolean issuedTo(final Ticket ticket, final String username) {
        return ticket instanceof final OAuth20Token token && Optional.ofNullable(token.getAuthentication())
            .filter(authentication -> Strings.CI.equals(authentication.getPrincipal().getId(), username))
            .isPresent();
    }

    protected @Nullable String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder.getObject()).decode(token);
    }
}
