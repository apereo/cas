package org.apereo.cas.mcp.oauth;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link CasMcpProtectedResource}, the CAS MCP server seen as an OAuth protected resource (RFC 9728): the
 * resource identifier that access tokens must be issued for, the location of its protected resource metadata, and the
 * {@code WWW-Authenticate} challenges of RFC 6750 that point clients to that metadata.
 *
 * @author Misagh Moayyed
 * @param resource    the resource identifier, which is the URL of the MCP endpoint
 * @param metadataUrl the URL of the protected resource metadata
 * @since 8.1.0
 */
public record CasMcpProtectedResource(String resource, String metadataUrl) {
    /**
     * The scope an access token needs to use the MCP server.
     */
    public static final String SCOPE_READ = "mcp:read";

    /**
     * The scope an access token needs, in addition to {@link #SCOPE_READ}, for its person to be granted the role of the
     * tools that change CAS. It is advertised only when those tools are turned on.
     */
    public static final String SCOPE_WRITE = "mcp:write";

    /**
     * The well-known path of protected resource metadata, followed by the path of the MCP endpoint.
     */
    public static final String WELL_KNOWN_PATH = "/.well-known/oauth-protected-resource";

    /**
     * Describe the MCP endpoint of this CAS server, under the CAS server prefix.
     *
     * @param casProperties the CAS settings
     * @param endpoint      the path of the MCP endpoint
     * @return the protected resource
     */
    public static CasMcpProtectedResource of(final CasConfigurationProperties casProperties, final String endpoint) {
        val prefix = Strings.CS.removeEnd(casProperties.getServer().getPrefix(), "/");
        val path = Strings.CS.prependIfMissing(endpoint, "/");
        return new CasMcpProtectedResource(prefix + path, prefix + WELL_KNOWN_PATH + path);
    }

    /**
     * Build a {@code Bearer} challenge that names the required scope and the protected resource metadata, with an
     * error when a token was presented and refused.
     *
     * @param error       the error code, such as {@code invalid_token}, or null when no token was presented
     * @param description the error description, or null
     * @return the value of the {@code WWW-Authenticate} header
     */
    public String challenge(@Nullable final String error, @Nullable final String description) {
        val parameters = new ArrayList<String>();
        if (StringUtils.isNotBlank(error)) {
            parameters.add("error=\"%s\"".formatted(error));
        }
        if (StringUtils.isNotBlank(description)) {
            parameters.add("error_description=\"%s\"".formatted(description));
        }
        parameters.add("scope=\"%s\"".formatted(SCOPE_READ));
        parameters.add("resource_metadata=\"%s\"".formatted(metadataUrl));
        return "Bearer " + String.join(", ", parameters);
    }

    /**
     * Whether one of the given resource indicators or audiences identifies this MCP server. Scheme and host are compared
     * without regard to case, as the MCP authorization specification asks; the rest must be identical.
     *
     * @param audiences the resources or audiences of an access token
     * @return true if the token was issued for this MCP server
     */
    public boolean isIssuedFor(final Collection<String> audiences) {
        val expected = canonicalize(resource);
        return audiences.stream().filter(StringUtils::isNotBlank).map(CasMcpProtectedResource::canonicalize).anyMatch(expected::equals);
    }

    private static String canonicalize(final String uri) {
        try {
            val parsed = new URI(uri);
            if (parsed.getScheme() == null || parsed.getHost() == null) {
                return uri;
            }
            val builder = new StringBuilder(parsed.getScheme().toLowerCase(Locale.ROOT)).append("://");
            if (parsed.getRawUserInfo() != null) {
                builder.append(parsed.getRawUserInfo()).append('@');
            }
            builder.append(parsed.getHost().toLowerCase(Locale.ROOT));
            if (parsed.getPort() != -1) {
                builder.append(':').append(parsed.getPort());
            }
            builder.append(Objects.toString(parsed.getRawPath(), StringUtils.EMPTY));
            if (parsed.getRawQuery() != null) {
                builder.append('?').append(parsed.getRawQuery());
            }
            return builder.toString();
        } catch (final URISyntaxException e) {
            return uri;
        }
    }
}
