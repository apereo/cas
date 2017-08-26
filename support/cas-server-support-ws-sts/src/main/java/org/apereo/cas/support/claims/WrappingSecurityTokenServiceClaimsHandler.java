package org.apereo.cas.support.claims;

import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.cxf.sts.token.realm.RealmSupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link WrappingSecurityTokenServiceClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WrappingSecurityTokenServiceClaimsHandler implements ClaimsHandler, RealmSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(WrappingSecurityTokenServiceClaimsHandler.class);
    private final String realm;
    private final String issuer;

    public WrappingSecurityTokenServiceClaimsHandler(final String realm, final String issuer) {
        this.realm = realm;
        this.issuer = issuer;
    }

    @Override
    public List<URI> getSupportedClaimTypes() {
        return WSFederationClaims.ALL_CLAIMS
                .stream()
                .map(c -> UriBuilder.fromUri(c.getUri()).build())
                .collect(Collectors.toList());
    }

    @Override
    public ProcessedClaimCollection retrieveClaimValues(final ClaimCollection claims, final ClaimsParameters parameters) {
        if (parameters.getRealm() == null || !parameters.getRealm().equalsIgnoreCase(this.realm)) {
            LOGGER.warn("Realm [{}] doesn't match with configured realm [{}]", parameters.getRealm(), this.realm);
            return new ProcessedClaimCollection();
        }
        if (parameters.getPrincipal() == null) {
            LOGGER.warn("No principal could be identified in the claim parameters request");
            return new ProcessedClaimCollection();
        }
        if (claims == null || claims.isEmpty()) {
            LOGGER.warn("No claims are available to process");
            return new ProcessedClaimCollection();
        }

        final ProcessedClaimCollection claimCollection = new ProcessedClaimCollection();
        claims.stream()
                .map(requestClaim -> {
                    final ProcessedClaim claim = new ProcessedClaim();
                    claim.setClaimType(requestClaim.getClaimType());
                    claim.setIssuer(this.issuer);
                    claim.setOriginalIssuer(this.issuer);
                    claim.setValues(requestClaim.getValues());
                    return claim;
                })
                .forEach(claimCollection::add);

        return claimCollection;
    }

    @Override
    public List<String> getSupportedRealms() {
        return CollectionUtils.wrap(this.realm);
    }

    @Override
    public String getHandlerRealm() {
        return this.realm;
    }
}
