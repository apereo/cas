package org.apereo.cas.support.oauth.web.mgmt;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

/**
 * This is {@link OAuth20TokenManagementEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "oauthTokens", defaultAccess = Access.NONE)
@Slf4j
public class OAuth20TokenManagementEndpoint extends BaseCasActuatorEndpoint {

    private final ObjectProvider<@NonNull TicketRegistry> ticketRegistry;

    private final ObjectProvider<@NonNull JwtBuilder> accessTokenJwtBuilder;

    public OAuth20TokenManagementEndpoint(final CasConfigurationProperties casProperties,
                                          final ObjectProvider<@NonNull TicketRegistry> ticketRegistry,
                                          final ObjectProvider<@NonNull JwtBuilder> accessTokenJwtBuilder) {
        super(casProperties);
        this.ticketRegistry = ticketRegistry;
        this.accessTokenJwtBuilder = accessTokenJwtBuilder;
    }

    /**
     * Gets access tokens.
     *
     * @return the access tokens
     */
    @ReadOperation
    @Operation(summary = "Get access and/or refresh tokens")
    public Collection<Ticket> getTokens() {
        return ticketRegistry.getObject().getTickets(ticket -> (ticket instanceof OAuth20AccessToken || ticket instanceof OAuth20RefreshToken) && !ticket.isExpired())
            .sorted(Comparator.comparing(Ticket::getId))
            .collect(Collectors.toList());
    }


    /**
     * Gets access token.
     *
     * @param token the token id
     * @return the access token
     */
    @ReadOperation
    @Operation(summary = "Get single token by id", parameters = @Parameter(name = "token", required = true, description = "The token id"))
    public Ticket getToken(@Selector final String token) {
        try {
            val ticketId = extractAccessTokenFrom(token);
            return ticketRegistry.getObject().getTicket(ticketId, Ticket.class);
        } catch (final Exception e) {
            LOGGER.debug("Ticket [{}] is has expired or cannot be found", token);
            return null;
        }
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
        if (ticket != null) {
            ticketRegistry.getObject().deleteTicket(ticket.getId());
        }
    }

    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder.getObject()).decode(token);
    }
}
