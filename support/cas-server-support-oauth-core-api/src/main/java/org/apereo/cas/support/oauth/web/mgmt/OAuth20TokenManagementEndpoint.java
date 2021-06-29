package org.apereo.cas.support.oauth.web.mgmt;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20TokenManagementEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Endpoint(id = "oauthTokens", enableByDefault = false)
@Slf4j
public class OAuth20TokenManagementEndpoint extends BaseCasActuatorEndpoint {

    private final CentralAuthenticationService centralAuthenticationService;

    private final JwtBuilder accessTokenJwtBuilder;

    public OAuth20TokenManagementEndpoint(final CasConfigurationProperties casProperties,
                                          final CentralAuthenticationService centralAuthenticationService,
                                          final JwtBuilder accessTokenJwtBuilder) {
        super(casProperties);
        this.centralAuthenticationService = centralAuthenticationService;
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
        return centralAuthenticationService.getTickets(ticket ->
            (ticket instanceof OAuth20AccessToken || ticket instanceof OAuth20RefreshToken) && !ticket.isExpired())
            .stream()
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
    @Operation(summary = "Get single token by id", parameters = {@Parameter(name = "token", required = true)})
    public Ticket getToken(@Selector final String token) {
        try {
            val ticketId = extractAccessTokenFrom(token);
            return centralAuthenticationService.getTicket(ticketId, Ticket.class);
        } catch (final Exception e) {
            LOGGER.debug("Ticket [{}] is has expired or cannot be found", token);
            return null;
        }
    }

    /**
     * Delete access token.
     *
     * @param ticketId the ticket id
     */
    @DeleteOperation
    @Operation(summary = "Delete token by id", parameters = {@Parameter(name = "ticketId", required = true)})
    public void deleteToken(@Selector final String ticketId) {
        val ticket = getToken(ticketId);
        if (ticket != null) {
            centralAuthenticationService.deleteTicket(ticket.getId());
        }
    }

    private String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .decode(token);
    }
}
