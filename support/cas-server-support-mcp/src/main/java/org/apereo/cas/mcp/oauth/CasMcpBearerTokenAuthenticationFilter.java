package org.apereo.cas.mcp.oauth;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.model.support.mcp.McpOAuthProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasMcpBearerTokenAuthenticationFilter}. It authenticates requests to the MCP endpoint that carry an OAuth
 * access token issued by CAS in the {@code Authorization: Bearer} header, as the MCP authorization specification and
 * RFC 6750 describe; requests without one go on to Basic authentication.
 * <p>
 * The token must be found in the ticket registry (by its identifier, or decoded when it is a JWT), must not be expired
 * or bound to a DPoP key or client certificate, must have been issued for the MCP endpoint as a resource or audience,
 * and the application it was issued to must still be allowed. Such a token is refused with {@code 401 invalid_token}.
 * A valid token without the {@code mcp:read} scope is refused with {@code 403 insufficient_scope}. The caller is
 * granted the {@code MCP_ADMIN} and {@code MCP_OPERATOR} roles as {@link #grantRoles(OAuth20AccessToken)} describes;
 * without them the caller may connect and list tools, and tool calls are refused. The security context is kept in a
 * request attribute only, so that asynchronous dispatches of the same request see it, and never in the HTTP session:
 * every request must present the token again.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasMcpBearerTokenAuthenticationFilter extends OncePerRequestFilter {
    /**
     * The role that lets a caller use the tools.
     */
    public static final String ROLE_MCP_ADMIN = "ROLE_MCP_ADMIN";

    /**
     * The role that lets a caller use the tools that change CAS.
     */
    public static final String ROLE_MCP_OPERATOR = "ROLE_MCP_OPERATOR";

    private static final String BEARER_PREFIX = "Bearer ";

    private final RequestMatcher endpoint;

    private final CasMcpProtectedResource protectedResource;

    private final McpOAuthProperties properties;

    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    private final PrincipalResolver principalResolver;

    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return !endpoint.matches(request) || bearerToken(request) == null;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {
        val accessToken = resolveAccessToken(Objects.requireNonNull(bearerToken(request)));
        if (accessToken == null) {
            reject(response, HttpStatus.UNAUTHORIZED, "invalid_token", "The access token is unknown or has expired");
            return;
        }
        val refusal = verify(accessToken);
        if (refusal != null) {
            LOGGER.debug("Access token for client [{}] is refused: [{}]", accessToken.getClientId(), refusal);
            reject(response, HttpStatus.UNAUTHORIZED, "invalid_token", refusal);
            return;
        }
        if (!accessToken.getScopes().contains(CasMcpProtectedResource.SCOPE_READ)) {
            reject(response, HttpStatus.FORBIDDEN, "insufficient_scope",
                "The access token does not grant the %s scope".formatted(CasMcpProtectedResource.SCOPE_READ));
            return;
        }
        val principal = accessToken.getAuthentication().getPrincipal();
        val authorities = grantRoles(accessToken);
        val authentication = new CasMcpBearerTokenAuthentication(principal.getId(), accessToken.getClientId(),
            accessToken.getScopes(), authorities);
        val strategy = SecurityContextHolder.getContextHolderStrategy();
        val context = strategy.createEmptyContext();
        context.setAuthentication(authentication);
        strategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        filterChain.doFilter(request, response);
    }

    /**
     * Find the problem with a token that was found and has not expired, if any.
     *
     * @param accessToken the access token
     * @return the reason to refuse the token, or null when it may be used
     */
    protected @Nullable String verify(final OAuth20AccessToken accessToken) {
        if (accessToken.isExpired()) {
            return "The access token has expired";
        }
        if (accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)
            || OAuth20Utils.getBoundCertificateThumbprint(accessToken).isPresent()) {
            return "Sender-constrained access tokens are not accepted";
        }
        val audiences = new LinkedHashSet<>(accessToken.getResources());
        audiences.addAll(accessToken.getAudience());
        if (!protectedResource.isIssuedFor(audiences)) {
            return "The access token was not issued for " + protectedResource.resource();
        }
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
        if (registeredService == null) {
            return "The application of the access token is not registered";
        }
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        } catch (final UnauthorizedServiceException e) {
            return "The application of the access token is not allowed";
        }
        return null;
    }

    /**
     * The roles of the person behind the token. {@code MCP_ADMIN} is granted when the person has every attribute in
     * {@link McpOAuthProperties#getRequiredAttributes()}, and {@code MCP_OPERATOR}, which the tools that change CAS
     * require, when the token carries the {@code mcp:write} scope and the person has every attribute in
     * {@link McpOAuthProperties#getOperatorRequiredAttributes()}. Without attributes defined, the role is granted to
     * nobody. The attributes are those CAS resolves for the person now, through the principal resolvers, rather than
     * those released to the client in the token, so the roles do not depend on the client's attribute release policy and
     * follow changes to the person's attributes; when nothing can be resolved, the attributes of the token's principal are used.
     *
     * @param accessToken the access token
     * @return the granted roles
     */
    protected List<SimpleGrantedAuthority> grantRoles(final OAuth20AccessToken accessToken) {
        val administrator = properties.getRequiredAttributes();
        val operator = accessToken.getScopes().contains(CasMcpProtectedResource.SCOPE_WRITE)
            ? properties.getOperatorRequiredAttributes()
            : Map.<String, String>of();
        if (administrator.isEmpty() && operator.isEmpty()) {
            return List.of();
        }
        val principal = resolvePrincipal(accessToken.getAuthentication().getPrincipal());
        val roles = new ArrayList<SimpleGrantedAuthority>();
        if (hasAttributes(principal, administrator)) {
            roles.add(new SimpleGrantedAuthority(ROLE_MCP_ADMIN));
        }
        if (hasAttributes(principal, operator)) {
            roles.add(new SimpleGrantedAuthority(ROLE_MCP_OPERATOR));
        }
        return roles;
    }

    private static boolean hasAttributes(final Principal principal, final Map<String, String> required) {
        return !required.isEmpty() && required.entrySet().stream().allMatch(entry -> {
            val pattern = RegexUtils.createPattern(entry.getValue());
            return principal.getAttributes().getOrDefault(entry.getKey(), List.of())
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .anyMatch(value -> RegexUtils.matches(pattern, value));
        });
    }

    /**
     * Find the access token in the ticket registry, by its identifier or, for a JWT, by the identifier it carries.
     *
     * @param token the token as presented
     * @return the access token, or null when it is unknown
     */
    protected @Nullable OAuth20AccessToken resolveAccessToken(final String token) {
        if (token.startsWith(OAuth20AccessToken.PREFIX + '-')) {
            return findAccessToken(token);
        }
        for (val builder : accessTokenJwtBuilder.stream().toList()) {
            try {
                val id = OAuth20JwtAccessTokenEncoder.toDecodableCipher(builder).decode(token);
                val accessToken = StringUtils.isNotBlank(id) && !token.equals(id) ? findAccessToken(id) : null;
                if (accessToken != null) {
                    return accessToken;
                }
            } catch (final Exception e) {
                LOGGER.debug("Unable to decode the access token as a JWT");
            }
        }
        return null;
    }

    private Principal resolvePrincipal(final Principal tokenPrincipal) {
        val credential = new BasicIdentifiableCredential(tokenPrincipal.getId());
        val resolved = FunctionUtils.doAndHandle(() -> principalResolver.resolve(credential,
            Optional.of(tokenPrincipal), Optional.empty(), Optional.empty()));
        return resolved == null || resolved instanceof NullPrincipal ? tokenPrincipal : resolved;
    }

    private @Nullable OAuth20AccessToken findAccessToken(final String id) {
        try {
            return ticketRegistry.getTicket(id, OAuth20AccessToken.class);
        } catch (final InvalidTicketException | ClassCastException e) {
            LOGGER.debug("Access token cannot be found in the ticket registry");
            return null;
        }
    }

    private void reject(final HttpServletResponse response, final HttpStatus status,
                        final String error, final String description) {
        SecurityContextHolder.clearContext();
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, protectedResource.challenge(error, description));
        response.setStatus(status.value());
    }

    private static @Nullable String bearerToken(final HttpServletRequest request) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!Strings.CI.startsWith(header, BEARER_PREFIX)) {
            return null;
        }
        return StringUtils.defaultIfBlank(header.substring(BEARER_PREFIX.length()).trim(), null);
    }
}
