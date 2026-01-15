package org.apereo.cas.oidc.assurance;

import module java.base;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link DefaultAssuranceVerifiedClaimsProducer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultAssuranceVerifiedClaimsProducer implements AssuranceVerifiedClaimsProducer {
    static final String CLAIM_NAME_VERIFIED_CLAIMS = "verified_claims";
    static final String CLAIM_NAME_CLAIMS = "claims";

    protected final AssuranceVerificationSource assuranceVerificationSource;
    protected final OidcServerDiscoverySettings discoverySettings;

    @Override
    public Map<String, Object> produce(final JwtClaims jwtClaims, final String claimName, final String trustFramework) {
        if (!discoverySettings.isVerifiedClaimsSupported()
            || (discoverySettings.getTrustFrameworksSupported() != null
            && !discoverySettings.getTrustFrameworksSupported().contains(trustFramework))) {
            return Map.of();
        }

        val source = assuranceVerificationSource.findByTrustFramework(trustFramework);
        return source
            .map(Unchecked.function(verification -> {
                val evidenceList = verification.getEvidence();
                if (evidenceList != null) {
                    evidenceList.removeIf(evidence -> discoverySettings.getEvidenceSupported() != null
                        && !discoverySettings.getEvidenceSupported().contains(evidence.getType()));
                    evidenceList.removeIf(evidence -> discoverySettings.getDocumentsSupported() != null
                        && !discoverySettings.getDocumentsSupported().contains(evidence.getDocument().getType()));
                    evidenceList.removeIf(evidence -> discoverySettings.getDocumentsSupported() != null
                        && !discoverySettings.getDocumentsSupported().contains(evidence.getDocumentDetails().getType()));
                    evidenceList.removeIf(evidence -> discoverySettings.getDocumentsValidationMethodsSupported() != null
                        && !discoverySettings.getDocumentsValidationMethodsSupported().contains(evidence.getValidationMethod().getType()));
                    evidenceList.removeIf(evidence -> discoverySettings.getDocumentsVerificationMethodsSupported() != null
                        && !discoverySettings.getDocumentsVerificationMethodsSupported().contains(evidence.getVerificationMethod().getType()));
                    evidenceList.removeIf(evidence -> discoverySettings.getElectronicRecordsSupported() != null
                        && !discoverySettings.getElectronicRecordsSupported().contains(evidence.getRecord().getType()));

                }
                val finalVerification = JwtClaims.parse(verification.toJson()).getClaimsMap();
                val verifiedClaims = (Map) Objects.requireNonNullElseGet(jwtClaims.getClaimValue(CLAIM_NAME_VERIFIED_CLAIMS), HashMap::new);
                verifiedClaims.put("verification", finalVerification);
                val currentClaimValue = jwtClaims.getClaimValue(claimName);
                val claims = (Map) Objects.requireNonNullElseGet(verifiedClaims.get(CLAIM_NAME_CLAIMS), HashMap::new);

                if (discoverySettings.getClaimsInVerifiedClaimsSupported() == null
                    || discoverySettings.getClaimsInVerifiedClaimsSupported().contains(claimName)) {
                    claims.put(claimName, currentClaimValue);
                    jwtClaims.unsetClaim(claimName);
                }
                verifiedClaims.put(CLAIM_NAME_CLAIMS, claims);
                jwtClaims.setClaim(CLAIM_NAME_VERIFIED_CLAIMS, verifiedClaims);
                return jwtClaims.getClaimsMap();
            }))
            .orElseGet(Map::of);
    }
}
