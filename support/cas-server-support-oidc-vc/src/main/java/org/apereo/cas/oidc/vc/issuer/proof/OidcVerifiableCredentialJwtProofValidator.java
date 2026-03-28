package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialJwtProofValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialJwtProofValidator implements OidcVerifiableCredentialProofValidator {
    private static final int SECONDS_IN_FUTURE = 30;
    private static final int MINUTES_IN_PAST = 5;

    private final CasConfigurationProperties casProperties;
    private final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

    @Override
    public VerifiableCredentialProofResult validate(final OidcVerifiableCredentialRequest request) throws Exception {
        val proof = request.getProof();
        val signedJwt = SignedJWT.parse(proof.getJwt());
        val holderJwk = signedJwt.getHeader().getJWK();

        verifySignature(signedJwt, holderJwk);
        verifyAlgorithm(signedJwt, holderJwk);
        verifyAudience(signedJwt);
        verifyFreshness(signedJwt);
        verifyNonce(signedJwt);

        val claims = signedJwt.getJWTClaimsSet();
        return new VerifiableCredentialProofResult(
            "jwt",
            claims.getJWTID(),
            claims.getSubject(),
            holderJwk
        );
    }

    protected void verifyNonce(final SignedJWT signedJwt) throws Exception {
        val claims = signedJwt.getJWTClaimsSet();
        val nonce = claims.getStringClaim("nonce");
        if (nonce == null || !oidcVerifiableCredentialNonceService.exists(nonce)) {
            throw new IllegalArgumentException("Proof nonce is invalid or missing");
        }
        oidcVerifiableCredentialNonceService.remove(nonce);
    }

    protected void verifySignature(final SignedJWT signedJwt, final JWK holderJwk) throws Exception {
        JWSVerifier verifier = null;
        if (holderJwk instanceof final RSAKey rsaKey) {
            verifier = new RSASSAVerifier(rsaKey);
        } else if (holderJwk instanceof final ECKey ecKey) {
            verifier = new ECDSAVerifier(ecKey);
        }
        if (verifier == null || !signedJwt.verify(verifier)) {
            throw new IllegalArgumentException("Proof JWT signature validation failed");
        }
    }

    protected void verifyAudience(final SignedJWT signedJwt) throws ParseException {
        val audiences = signedJwt.getJWTClaimsSet().getAudience();
        val credentialIssuer = casProperties.getAuthn().getOidc().getCore().getIssuer();
        if (audiences == null || !audiences.contains(credentialIssuer)) {
            throw new IllegalArgumentException("Proof audience does not match credential issuer");
        }
    }

    protected void verifyAlgorithm(final SignedJWT signedJwt, final JWK holderJwk) {
        val alg = signedJwt.getHeader().getAlgorithm();
        if (alg == null || Algorithm.NONE.equals(alg)) {
            throw new IllegalArgumentException("Proof JWT algorithm is invalid");
        }
        if (holderJwk instanceof RSAKey && !JWSAlgorithm.Family.RSA.contains(alg)) {
            throw new IllegalArgumentException("Proof JWT algorithm does not match RSA holder key");
        }
        if (holderJwk instanceof ECKey && !JWSAlgorithm.Family.EC.contains(alg)) {
            throw new IllegalArgumentException("Proof JWT algorithm does not match EC holder key");
        }
    }

    protected void verifyFreshness(final SignedJWT signedJwt) throws ParseException {
        val claims = signedJwt.getJWTClaimsSet();
        val issuedAt = claims.getIssueTime();
        if (issuedAt == null) {
            throw new IllegalArgumentException("Proof JWT is missing iat");
        }
        val now = Instant.now(Clock.systemUTC());
        val iat = issuedAt.toInstant();
        if (iat.isAfter(now.plusSeconds(SECONDS_IN_FUTURE))) {
            throw new IllegalArgumentException("Proof iat is in the future");
        }
        if (iat.isBefore(now.minus(Duration.ofMinutes(MINUTES_IN_PAST)))) {
            throw new IllegalArgumentException("Proof JWT is too old");
        }
    }
}
