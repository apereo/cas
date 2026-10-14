package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofException;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import com.authlete.cose.COSEEC2Key;
import com.authlete.cose.COSEKey;
import com.authlete.mdoc.IssuerSignedBuilder;
import com.authlete.mdoc.ValidityInfo;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.EllipticCurves;

/**
 * This is {@link OidcVerifiableCredentialMsoMdocEncoder}, which issues an ISO/IEC 18013-5 mdoc in the
 * {@code mso_mdoc} format of OpenID4VCI 1.0, appendix A.2. The credential is the base64url encoding of
 * the CBOR {@code IssuerSigned} structure: the claims as salted items grouped by name space, and the
 * issuer's signature over their digests, the document type, the validity period and the holder's device
 * key, carried in the mobile security object of a {@code COSE_Sign1} structure.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialMsoMdocEncoder extends BaseOidcVerifiableCredentialEncoder {
    private final OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats format =
        OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.MSO_MDOC;

    public OidcVerifiableCredentialMsoMdocEncoder(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public String encode(final OidcVerifiableCredentialValidationContext context,
                         final OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof)
            throws Throwable {
        val authentication = Objects.requireNonNull(context.accessToken().getAuthentication());
        val principal = configurationContext.getPrincipalResolver().resolve(
            new BasicIdentifiableCredential(authentication.getPrincipal().getId()));
        val verifiableClaims = produceClaims(Objects.requireNonNull(principal), context);
        val configurationId = context.resolveConfigurationId();
        val configuration = resolveConfiguration(configurationId);
        val doctype = resolveDocumentType(configurationId, configuration);

        val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), context.accessToken().getClientId(), OidcRegisteredService.class));
        val signingKey = resolveIssuerSigningKey(configuration, registeredService);
        val certificateChain = resolveCertificateChain(signingKey);
        if (certificateChain.isEmpty()) {
            throw new IllegalStateException("The mso_mdoc format requires the issuer signing key [%s] to carry an X.509 certificate chain"
                .formatted(signingKey.getKeyId()));
        }

        val signed = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        val issuerSigned = new IssuerSignedBuilder()
            .setDocType(doctype)
            .setClaims(groupClaimsByNamespace(doctype, configuration, verifiableClaims))
            .setValidityInfo(new ValidityInfo(signed, signed, signed.plus(resolveCredentialValidity(configurationId))))
            .setDeviceKey(toDeviceKey(proof.holderJwk()))
            .setIssuerKey(toIssuerKey(signingKey))
            .setIssuerCertChain(certificateChain)
            .build();
        return Base64URL.encode(issuerSigned.encode()).toString();
    }

    protected String resolveDocumentType(final String configurationId,
                                         final OidcVerifiableCredentialConfigurationProperties configuration) {
        if (StringUtils.isBlank(configuration.getDoctype())) {
            throw new IllegalStateException("Credential configuration [%s] uses the mso_mdoc format but defines no doctype"
                .formatted(configurationId));
        }
        return configuration.getDoctype();
    }

    /**
     * Claims of an mdoc are grouped by name space. A claim names its own, or falls in the one named after the document type.
     *
     * @param doctype       the document type
     * @param configuration the credential configuration
     * @param claims        the claims
     * @return the claims by name space
     */
    protected Map<String, Object> groupClaimsByNamespace(final String doctype,
                                                         final OidcVerifiableCredentialConfigurationProperties configuration,
                                                         final Map<String, Object> claims) {
        val namespaces = new LinkedHashMap<String, Map<String, Object>>();
        claims.forEach((claimName, claimValue) -> {
            val namespace = StringUtils.defaultIfBlank(configuration.getClaims().get(claimName).getNamespace(), doctype);
            namespaces.computeIfAbsent(namespace, _ -> new LinkedHashMap<>()).put(claimName, claimValue);
        });
        return new LinkedHashMap<>(namespaces);
    }

    /**
     * The issuer signs the mobile security object with an elliptic curve key, whose curve sets the algorithm
     * (ISO/IEC 18013-5, section 9.1.2.4): {@code ES256} for P-256, {@code ES384} for P-384 and {@code ES512} for P-521.
     * That algorithm must be one the credential configuration advertises and the service allows.
     *
     * @param configuration     the credential configuration
     * @param registeredService the registered service
     * @return the issuer signing key
     * @throws Throwable the throwable
     */
    protected PublicJsonWebKey resolveIssuerSigningKey(final OidcVerifiableCredentialConfigurationProperties configuration,
                                                       final OidcRegisteredService registeredService)
            throws Throwable {
        val signingKey = configurationContext.getIdTokenSigningAndEncryptionService()
            .getJsonWebKeySigningKey(Optional.of(registeredService));
        Objects.requireNonNull(signingKey, "No signing key is available to sign the verifiable credential");
        Objects.requireNonNull(signingKey.getPrivateKey(), "The credential signing key has no private key");
        if (!(signingKey instanceof final EllipticCurveJsonWebKey ecKey)) {
            throw new IllegalStateException("The mso_mdoc format requires an elliptic curve signing key, but key [%s] is %s"
                .formatted(signingKey.getKeyId(), signingKey.getKeyType()));
        }
        val algorithm = switch (ecKey.getCurveName()) {
            case EllipticCurves.P_256 -> "ES256";
            case EllipticCurves.P_384 -> "ES384";
            case EllipticCurves.P_521 -> "ES512";
            default -> throw new IllegalStateException("The mso_mdoc format does not support the curve %s of signing key [%s]"
                .formatted(ecKey.getCurveName(), signingKey.getKeyId()));
        };
        val supported = resolveSupportedSigningAlgorithms(configuration, registeredService);
        if (!supported.contains(algorithm)) {
            throw new IllegalStateException("Signing key [%s] signs with %s, which is not among the credential signing algorithms %s"
                .formatted(signingKey.getKeyId(), algorithm, supported));
        }
        return signingKey;
    }

    protected COSEEC2Key toIssuerKey(final PublicJsonWebKey signingKey) throws Throwable {
        val parameters = new LinkedHashMap<>(signingKey.toParams(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE));
        parameters.remove(JsonWebKey.ALGORITHM_PARAMETER);
        return (COSEEC2Key) COSEKey.fromJwk(parameters);
    }

    /**
     * The holder's device key, from the key its proof was signed with. An mdoc authenticates the device with ECDSA or EdDSA
     * (ISO/IEC 18013-5, section 9.1.3), so an RSA key cannot be bound.
     *
     * @param holderJwk the holder key
     * @return the device key
     * @throws Throwable the throwable
     */
    protected COSEKey toDeviceKey(final JWK holderJwk) throws Throwable {
        if (!(holderJwk instanceof ECKey) && !(holderJwk instanceof OctetKeyPair)) {
            throw OidcVerifiableCredentialProofException.invalidProof(
                "The mso_mdoc format binds an elliptic curve or EdDSA device key, but the proof key is %s"
                    .formatted(holderJwk.getKeyType()));
        }
        val parameters = new LinkedHashMap<>(holderJwk.toPublicJWK().toJSONObject());
        parameters.remove(JsonWebKey.ALGORITHM_PARAMETER);
        return COSEKey.fromJwk(parameters);
    }
}
