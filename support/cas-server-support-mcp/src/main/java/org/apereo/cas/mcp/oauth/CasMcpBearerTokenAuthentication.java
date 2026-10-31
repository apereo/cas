package org.apereo.cas.mcp.oauth;

import module java.base;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * This is {@link CasMcpBearerTokenAuthentication}, a caller of the MCP server authenticated with an OAuth access token
 * issued by CAS. The token itself is not kept.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class CasMcpBearerTokenAuthentication extends AbstractAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 3871904475233290168L;

    private final String username;

    private final String clientId;

    private final Set<String> scopes;

    public CasMcpBearerTokenAuthentication(final String username, final String clientId, final Collection<String> scopes,
                                           final Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.clientId = clientId;
        this.scopes = new LinkedHashSet<>(scopes);
        super.setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
