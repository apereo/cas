package org.apereo.cas.support.claims;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.cxf.sts.token.realm.RealmSupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * This is {@link WrappingSecurityTokenServiceClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class WrappingSecurityTokenServiceClaimsHandler implements ClaimsHandler, RealmSupport {

    private final String handlerRealm;

    private final String issuer;

    @Override
    public List<URI> getSupportedClaimTypes() {
        return WSFederationClaims.ALL_CLAIMS.stream().map(c -> UriBuilder.fromUri(c.getUri()).build()).collect(Collectors.toList());
    }

    @Override
    public ProcessedClaimCollection retrieveClaimValues(final ClaimCollection claims, final ClaimsParameters parameters) {
        if (parameters.getRealm() == null || !parameters.getRealm().equalsIgnoreCase(this.handlerRealm)) {
            LOGGER.warn("Realm [{}] doesn't match with configured realm [{}]", parameters.getRealm(), this.handlerRealm);
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
        claims.stream().map(requestClaim -> {
            final ProcessedClaim claim = new ProcessedClaim();
            claim.setClaimType(requestClaim.getClaimType());
            claim.setIssuer(this.issuer);
            claim.setOriginalIssuer(this.issuer);
            claim.setValues(requestClaim.getValues());
            return claim;
        }).forEach(claimCollection::add);
        return claimCollection;
    }

    @Override
    public List<String> getSupportedRealms() {
        return CollectionUtils.wrap(this.handlerRealm);
    }
}
