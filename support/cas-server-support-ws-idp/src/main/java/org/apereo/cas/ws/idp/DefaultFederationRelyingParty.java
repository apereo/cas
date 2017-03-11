package org.apereo.cas.ws.idp;

import org.apereo.cas.ws.idp.api.FederationRelyingParty;

import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link DefaultFederationRelyingParty}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultFederationRelyingParty implements FederationRelyingParty {
    private String realm;
    private String protocol;
    private String displayName;
    private String description;
    private String tokenType;
    private long lifetime;
    private String role;
    private Collection<DefaultFederationClaim> claims = new HashSet<>();

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTokenType() {
        return tokenType;
    }

    @Override
    public long getLifetime() {
        return lifetime;
    }

    @Override
    public Collection<DefaultFederationClaim> getClaims() {
        return claims;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTokenType(final String tokenType) {
        this.tokenType = tokenType;
    }

    public void setLifetime(final long lifetime) {
        this.lifetime = lifetime;
    }

    public void setClaims(final Collection<DefaultFederationClaim> claims) {
        this.claims = claims;
    }
}
