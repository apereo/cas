package org.apereo.cas.support;

import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is {@link FileClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class FileClaimsHandler implements ClaimsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileClaimsHandler.class);
    
    private Map<String, Map<String, String>> userClaims;
    private List<URI> supportedClaims;
    private String realm;
    
    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }
    
    public void setUserClaims(final Map<String, Map<String, String>> userClaims) {
        this.userClaims = userClaims;
    }

    public Map<String, Map<String, String>> getUserClaims() {
        return userClaims;
    }

    public void setSupportedClaims(final List<URI> supportedClaims) {
        this.supportedClaims = supportedClaims;
    }

    @Override
    public List<URI> getSupportedClaimTypes() {
        return Collections.unmodifiableList(this.supportedClaims);
    }


    @Override
    public ProcessedClaimCollection retrieveClaimValues(final ClaimCollection claims,
                                                        final ClaimsParameters parameters) {

        if (parameters.getRealm() == null || !parameters.getRealm().equalsIgnoreCase(getRealm())) {
            LOGGER.debug("Realm {} doesn't match with configured realm {}", parameters.getRealm(), getRealm());
            return new ProcessedClaimCollection();
        }
        
        if (getUserClaims() == null || parameters.getPrincipal() == null) {
            return new ProcessedClaimCollection();
        }

        if (claims == null || claims.size() == 0) {
            return new ProcessedClaimCollection();
        }

        final Map<String, String> claimMap = getUserClaims().get(parameters.getPrincipal().getName());
        if (claimMap == null || claimMap.size() == 0) {
            return new ProcessedClaimCollection();
        }

        if (claims != null && claims.size() > 0) {
            final ProcessedClaimCollection claimCollection = new ProcessedClaimCollection();
            for (final Claim requestClaim : claims) {
                final String claimValue = claimMap.get(requestClaim.getClaimType().toString());
                if (claimValue != null) {
                    final ProcessedClaim claim = new ProcessedClaim();
                    claim.setClaimType(requestClaim.getClaimType());
                    claim.setIssuer("Test Issuer");
                    claim.setOriginalIssuer("Original Issuer");
                    claim.addValue(claimValue);
                    claimCollection.add(claim);
                }
            }
            return claimCollection;
        }
        return null;

    }
}
