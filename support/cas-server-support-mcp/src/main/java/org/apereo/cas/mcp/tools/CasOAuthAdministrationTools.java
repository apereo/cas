package org.apereo.cas.mcp.tools;

import module java.base;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This is {@link CasOAuthAdministrationTools}, available when OAuth is included in the build and the {@code mcp-write}
 * feature is turned on. Token ids are never returned.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasOAuthAdministrationTools {
    private final TicketRegistry ticketRegistry;

    /**
     * Revoke the OAuth access and refresh tokens of a person, of a client application, or of a person for one client.
     *
     * @param username the username, optional
     * @param clientId the client id, optional
     * @return the number of revoked tokens
     */
    @McpTool(description = "Revoke the OAuth and OpenID Connect access tokens and refresh tokens that CAS issued to a person, "
        + "to a client application, or to a person for one client, by removing them from the ticket registry. Clients "
        + "must then obtain new tokens; JWT access tokens are refused once their record is gone. Token ids are never "
        + "returned.",
        title = "Revoke OAuth Tokens",
        generateOutputSchema = true,
        annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true,
            idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasRole('MCP_OPERATOR')")
    public RevokedOAuthTokens revokeOAuthTokens(
        @McpToolParam(description = "Only tokens issued to this username (principal id)", required = false)
        @Nullable final String username,
        @McpToolParam(description = "Only tokens issued to this OAuth client id", required = false)
        @Nullable final String clientId) {
        if (StringUtils.isAllBlank(username, clientId)) {
            return new RevokedOAuthTokens(username, clientId, 0, 0, "A username or a client id is required");
        }
        val tokens = new ArrayList<Ticket>();
        try (val found = ticketRegistry.getTickets(ticket -> (ticket instanceof OAuth20AccessToken || ticket instanceof OAuth20RefreshToken)
            && matches((OAuth20Token) ticket, username, clientId))) {
            found.forEach(tokens::add);
        }
        tokens.forEach(token -> FunctionUtils.doUnchecked(() -> ticketRegistry.deleteTicket(token)));
        val accessTokens = (int) tokens.stream().filter(OAuth20AccessToken.class::isInstance).count();
        return new RevokedOAuthTokens(username, clientId, accessTokens, tokens.size() - accessTokens, null);
    }

    private static boolean matches(final OAuth20Token token, @Nullable final String username, @Nullable final String clientId) {
        val authentication = token.getAuthentication();
        val principal = authentication != null ? authentication.getPrincipal().getId() : null;
        return (StringUtils.isBlank(clientId) || Strings.CS.equals(token.getClientId(), clientId))
            && (StringUtils.isBlank(username) || Strings.CS.equals(principal, username));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RevokedOAuthTokens(@Nullable String username, @Nullable String clientId, int accessTokens, int refreshTokens,
                                     @Nullable String reason) {
    }
}
