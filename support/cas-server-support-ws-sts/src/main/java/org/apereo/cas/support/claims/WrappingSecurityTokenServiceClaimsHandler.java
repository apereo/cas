package org.apereo.cas.support.claims;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationClaims;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.cxf.sts.token.realm.RealmSupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link WrappingSecurityTokenServiceClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class WrappingSecurityTokenServiceClaimsHandler implements ClaimsHandler, RealmSupport {

    private final String handlerRealm;

    private final String issuer;

    @Override
    public List<String> getSupportedClaimTypes() {
        return WSFederationClaims.ALL_CLAIMS.stream()
            .map(WSFederationClaims::getUri)
            .collect(Collectors.toList());
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
        val claimCollection = new ProcessedClaimCollection();
        claims.stream().map(c -> createProcessedClaim(c, parameters)).forEach(claimCollection::add);
        return claimCollection;
    }

    /**
     * Create processed claim processed claim.
     *
     * @param requestClaim the request claim
     * @param parameters   the parameters
     * @return the processed claim
     */
    protected ProcessedClaim createProcessedClaim(final Claim requestClaim, final ClaimsParameters parameters) {
        val claim = new ProcessedClaim();
        claim.setClaimType(createProcessedClaimType(requestClaim, parameters));
        claim.setIssuer(this.issuer);
        claim.setOriginalIssuer(this.issuer);
        claim.setValues(requestClaim.getValues());
        return claim;
    }

    /**
     * Create processed claim type uri.
     *
     * @param requestClaim the request claim
     * @param parameters   the parameters
     * @return the uri
     */
    protected String createProcessedClaimType(final Claim requestClaim, final ClaimsParameters parameters) {
        return requestClaim.getClaimType();
    }

    @Override
    public List<String> getSupportedRealms() {
        return CollectionUtils.wrap(this.handlerRealm);
    }
}
