package org.apereo.cas.ticket.idtoken;

import org.apereo.cas.audit.AuditableEntity;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.jose4j.jwt.JwtClaims;
import jakarta.annotation.Nonnull;

/**
 * This is {@link OidcIdToken}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public record OidcIdToken(String token, JwtClaims claims, String deviceSecret) implements AuditableEntity {
    @Nonnull
    @Override
    public String toString() {
        return this.token;
    }

    @Override
    public String getAuditablePrincipal() {
        return StringUtils.defaultIfBlank(claims.getClaimValueAsString("sub"), PrincipalResolver.UNKNOWN_USER);
    }
}
