package org.apereo.cas.vc.presentation;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.vc.presentation.OidcVerifiableCredentialPresentationRequestEndpointController.OidcVerifiableCredentialPresentationRequest.CredentialRequest;
import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORDecoder;
import com.authlete.cbor.CBORItem;
import com.authlete.cbor.CBORItemList;
import com.authlete.cbor.CBORNull;
import com.authlete.cbor.CBORPair;
import com.authlete.cbor.CBORPairList;
import com.authlete.cbor.CBORString;
import com.authlete.cbor.CBORTaggedItem;
import com.authlete.cbor.CBORValue;
import com.authlete.cose.COSEKey;
import com.authlete.cose.COSESign1;
import com.authlete.cose.COSEVerifier;
import com.authlete.cose.SigStructureBuilder;
import com.authlete.cose.constants.COSEAlgorithms;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.util.Base64URL;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jspecify.annotations.Nullable;
import java.security.cert.X509Certificate;

/**
 * This is {@link OidcVerifiableCredentialMdocPresentationVerifier}, which verifies an ISO/IEC 18013-5 mdoc presented
 * in the {@code mso_mdoc} format of OpenID4VP 1.0, appendix B.2: a base64url-encoded CBOR {@code DeviceResponse}
 * holding one document. The issuer's signature over the mobile security object must verify with one of CAS's own
 * signing keys, as for SD-JWT VC presentations, and the issuer-signed items must match its digests. The device
 * signature, made with the key the mobile security object binds, must cover the session transcript of this
 * request, which carries its nonce and client identifier or origin, so a presentation answers one request only.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialMdocPresentationVerifier {
    /**
     * COSE algorithms accepted for the device signature (ISO/IEC 18013-5, section 9.1.3.6), advertised as
     * {@code deviceauth_alg_values}.
     */
    public static final List<Integer> DEVICE_SIGNING_ALGORITHMS =
        List.of(COSEAlgorithms.ES256, COSEAlgorithms.ES384, COSEAlgorithms.ES512, COSEAlgorithms.EdDSA);

    /**
     * Algorithms an issuer may sign the mobile security object with (ISO/IEC 18013-5, section 9.1.2.4).
     */
    public static final Set<String> ISSUER_SIGNING_ALGORITHMS = Set.of("ES256", "ES384", "ES512", "EdDSA");

    private static final int ENCODED_CBOR_DATA_ITEM = 24;

    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    private final OidcConfigurationContext configurationContext;

    private final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> jsonWebKeystoreCache;

    /**
     * The handover of a presentation returned to the {@code response_uri} (OpenID4VP 1.0, appendix B.2.6.1).
     *
     * @param clientId      the {@code client_id} of the request, with its client identifier prefix
     * @param nonce         the nonce of the request
     * @param jwkThumbprint the SHA-256 JWK thumbprint of the key the response is encrypted to, or null
     * @param responseUri   the {@code response_uri} of the request
     * @return the handover
     */
    public static CBORItem buildRedirectHandover(final String clientId, final String nonce,
                                                 final byte @Nullable [] jwkThumbprint, final String responseUri) {
        return buildHandover("OpenID4VPHandover", new CBORItemList(
            new CBORString(clientId), new CBORString(nonce), toThumbprintItem(jwkThumbprint), new CBORString(responseUri)));
    }

    /**
     * The handover of a presentation returned through the Digital Credentials API (OpenID4VP 1.0, appendix B.2.6.2).
     *
     * @param origin        the origin of the relying party page
     * @param nonce         the nonce of the request
     * @param jwkThumbprint the SHA-256 JWK thumbprint of the key the response is encrypted to, or null
     * @return the handover
     */
    public static CBORItem buildDigitalCredentialsHandover(final String origin, final String nonce,
                                                           final byte @Nullable [] jwkThumbprint) {
        return buildHandover("OpenID4VPDCAPIHandover", new CBORItemList(
            new CBORString(origin), new CBORString(nonce), toThumbprintItem(jwkThumbprint)));
    }

    /**
     * Verify a presentation against its credential query.
     *
     * @param presentation    the base64url-encoded {@code DeviceResponse}
     * @param credentialQuery the credential query
     * @param handover        the handover of this request
     * @return the issuer-signed claims, by name space
     * @throws Throwable when the presentation does not verify
     */
    public Map<String, Object> verify(final String presentation, final CredentialRequest credentialQuery,
                                      final CBORItem handover)
            throws Throwable {
        val deviceResponse = asMap(decode(Base64URL.from(presentation).decode()), "DeviceResponse");
        require("1.0".equals(readValue(deviceResponse, "version")), "DeviceResponse version is not supported");
        require(readValue(deviceResponse, "status") instanceof final Number status && status.intValue() == 0,
            "DeviceResponse reports an error");
        require(deviceResponse.findByKey("documentErrors") == null, "DeviceResponse reports document errors");
        val documents = asList(find(deviceResponse, "documents"), "documents");
        require(documents.size() == 1, "DeviceResponse must hold exactly one document");
        val document = asMap(documents.getFirst(), "document");
        val docType = readValue(document, "docType");
        require(docType instanceof String && docType.equals(credentialQuery.getDoctypeValue()),
            "Document type does not satisfy the query");
        val configuration = resolveCredentialConfiguration(docType.toString());

        val issuerSigned = asMap(find(document, "issuerSigned"), "issuerSigned");
        val mso = verifyIssuerAuth(COSESign1.build(find(issuerSigned, "issuerAuth")), configuration);
        val msoValues = (Map<?, ?>) mso.parse();
        require("1.0".equals(msoValues.get("version")), "Mobile security object version is not supported");
        require("SHA-256".equals(msoValues.get("digestAlgorithm")), "Mobile security object digest algorithm is not supported");
        require(docType.equals(msoValues.get("docType")), "Mobile security object does not match the document type");
        verifyValidity((Map<?, ?>) msoValues.get("validityInfo"));

        val claims = verifyIssuerSignedItems(issuerSigned, (Map<?, ?>) msoValues.get("valueDigests"));
        val deviceKey = COSEKey.build(find(asMap(find(mso, "deviceKeyInfo"), "deviceKeyInfo"), "deviceKey"));
        verifyDeviceSignature(asMap(find(document, "deviceSigned"), "deviceSigned"), docType.toString(), handover, deviceKey);
        return claims;
    }

    /**
     * The issuer's signature over the mobile security object. The leaf of its {@code x5chain} must be the certificate of
     * one of CAS's signing keys, since CAS only verifies the credentials it issued, and its algorithm must be one the
     * credential configuration advertises.
     *
     * @param issuerAuth    the issuer signature
     * @param configuration the credential configuration
     * @return the mobile security object
     * @throws Throwable the throwable
     */
    protected CBORPairList verifyIssuerAuth(final COSESign1 issuerAuth,
                                            final OidcVerifiableCredentialConfigurationProperties configuration)
            throws Throwable {
        val algorithm = issuerAuth.getProtectedHeader().getAlg();
        require(algorithm instanceof Number, "Issuer signature names no algorithm in its protected header");
        val algorithmName = COSEAlgorithms.getNameByValue(((Number) algorithm).intValue());
        require(ISSUER_SIGNING_ALGORITHMS.contains(algorithmName)
            && configuration.getCredentialSigningAlgValuesSupported().contains(algorithmName), "Issuer signature algorithm is not allowed");
        val certificateChain = issuerAuth.getUnprotectedHeader().getX5Chain();
        require(certificateChain != null && !certificateChain.isEmpty(), "Issuer signature carries no certificate chain");
        require(new COSEVerifier(resolveIssuerKey(certificateChain.getFirst())).verify(issuerAuth), "Issuer signature is invalid");
        require(issuerAuth.getPayload() instanceof CBORByteArray, "Issuer signature carries no mobile security object");
        return asMap(decodeEmbedded(decode(((CBORByteArray) issuerAuth.getPayload()).getValue())), "MobileSecurityObject");
    }

    protected PublicKey resolveIssuerKey(final X509Certificate certificate) throws Throwable {
        val issuer = configurationContext.getIssuerService().determineIssuer(Optional.empty());
        val jwks = jsonWebKeystoreCache.get(new OidcJsonWebKeyCacheKey(issuer, OidcJsonWebKeyUsage.SIGNING));
        require(jwks != null, "Credential issuer has no signing keys");
        return jwks.getJsonWebKeys()
            .stream()
            .filter(PublicJsonWebKey.class::isInstance)
            .map(PublicJsonWebKey.class::cast)
            .filter(key -> key.getCertificateChain() != null && !key.getCertificateChain().isEmpty()
                && key.getCertificateChain().getFirst().equals(certificate))
            .map(PublicJsonWebKey::getPublicKey)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Credential was not issued by this issuer"));
    }

    protected void verifyValidity(final Map<?, ?> validityInfo) {
        require(validityInfo != null, "Mobile security object has no validity information");
        val now = Instant.now();
        val validFrom = Instant.parse(String.valueOf(validityInfo.get("validFrom")));
        val validUntil = Instant.parse(String.valueOf(validityInfo.get("validUntil")));
        require(!validFrom.isAfter(now.plus(CLOCK_SKEW)), "Credential is not yet valid");
        require(validUntil.isAfter(now.minus(CLOCK_SKEW)), "Credential has expired");
    }

    /**
     * The issuer-signed items, each of which must match the digest the mobile security object records for it.
     * The digest covers the item as it was presented, tag included (ISO/IEC 18013-5, section 9.1.2.5).
     *
     * @param issuerSigned the issuer-signed part of the document
     * @param valueDigests the digests of the mobile security object
     * @return the claims, by name space
     * @throws Throwable the throwable
     */
    protected Map<String, Object> verifyIssuerSignedItems(final CBORPairList issuerSigned, final Map<?, ?> valueDigests)
        throws Throwable {
        require(valueDigests != null, "Mobile security object has no value digests");
        val claims = new LinkedHashMap<String, Object>();
        val nameSpaces = issuerSigned.findByKey("nameSpaces");
        if (nameSpaces == null) {
            return claims;
        }
        for (val nameSpace : asMap(nameSpaces.getValue(), "nameSpaces").getPairs()) {
            val nameSpaceName = keyOf(nameSpace);
            val digests = (Map<?, ?>) valueDigests.get(nameSpaceName);
            require(digests != null, "Mobile security object has no digests for name space " + nameSpaceName);
            val elements = new LinkedHashMap<String, Object>();
            for (val itemBytes : asList(nameSpace.getValue(), nameSpaceName)) {
                val item = (Map<?, ?>) decodeEmbedded(itemBytes).parse();
                require(item.get("digestID") instanceof Number && item.get("elementIdentifier") instanceof String,
                    "Issuer-signed item is invalid");
                val digestId = ((Number) item.get("digestID")).intValue();
                val expectedDigest = digests.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() instanceof final Number key && key.intValue() == digestId)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
                require(expectedDigest instanceof final byte[] digest && MessageDigest.isEqual(digest,
                    MessageDigest.getInstance("SHA-256").digest(itemBytes.encode())), "Issuer-signed item does not match its digest");
                require(elements.putIfAbsent(item.get("elementIdentifier").toString(), item.get("elementValue")) == null,
                    "Issuer-signed items must not repeat an element");
            }
            claims.put(nameSpaceName, elements);
        }
        return claims;
    }

    /**
     * The device signature over {@code DeviceAuthenticationBytes}, a detached payload built from the session transcript of
     * this request, the document type and the device-signed name spaces (ISO/IEC 18013-5, section 9.1.3.4). OpenID4VP
     * leaves no reader engagement, so the session transcript is the handover alone.
     *
     * @param deviceSigned the device-signed part of the document
     * @param docType      the document type
     * @param handover     the handover of this request
     * @param deviceKey    the device key the mobile security object binds
     * @throws Throwable the throwable
     */
    protected void verifyDeviceSignature(final CBORPairList deviceSigned, final String docType,
                                         final CBORItem handover, final COSEKey deviceKey)
            throws Throwable {
        val deviceNameSpacesBytes = requireEncodedDataItem(find(deviceSigned, "nameSpaces"));
        val deviceAuth = asMap(find(deviceSigned, "deviceAuth"), "deviceAuth");
        require(deviceAuth.findByKey("deviceMac") == null, "Device authentication with a MAC is not supported");
        val deviceSignature = COSESign1.build(find(deviceAuth, "deviceSignature"));
        require(deviceSignature.getPayload() instanceof CBORNull, "Device signature must have a detached payload");
        val algorithm = deviceSignature.getProtectedHeader().getAlg();
        require(algorithm instanceof final Number value && DEVICE_SIGNING_ALGORITHMS.contains(value.intValue()),
            "Device signature algorithm is not allowed");

        val sessionTranscript = new CBORItemList(CBORNull.INSTANCE, CBORNull.INSTANCE, handover);
        val deviceAuthentication = new CBORItemList(new CBORString("DeviceAuthentication"),
            sessionTranscript, new CBORString(docType), deviceNameSpacesBytes);
        val deviceAuthenticationBytes = new CBORTaggedItem(ENCODED_CBOR_DATA_ITEM, new CBORByteArray(deviceAuthentication.encode()));
        val toBeSigned = new SigStructureBuilder()
            .signature1()
            .bodyAttributes(deviceSignature.getProtectedHeader())
            .payload(deviceAuthenticationBytes.encode())
            .build();
        require(COSEVerifier.verify(deviceKey.createPublicKey(), ((Number) algorithm).intValue(),
            toBeSigned.encode(), deviceSignature.getSignature().getValue()), "Device signature is invalid");
    }

    protected OidcVerifiableCredentialConfigurationProperties resolveCredentialConfiguration(final String docType) {
        return configurationContext.getCasProperties().getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations()
            .values()
            .stream()
            .filter(configuration -> configuration.getFormat()
                == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.MSO_MDOC)
            .filter(configuration -> docType.equals(configuration.getDoctype()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Document type is not configured"));
    }

    private static CBORItem buildHandover(final String type, final CBORItemList handoverInfo) {
        return FunctionUtils.doUnchecked(() -> new CBORItemList(new CBORString(type),
            new CBORByteArray(MessageDigest.getInstance("SHA-256").digest(handoverInfo.encode()))));
    }

    private static CBORItem toThumbprintItem(final byte @Nullable [] jwkThumbprint) {
        return jwkThumbprint == null ? CBORNull.INSTANCE : new CBORByteArray(jwkThumbprint);
    }

    private static CBORItem decode(final byte[] bytes) throws IOException {
        val item = new CBORDecoder(bytes).next();
        require(item != null, "CBOR data is empty");
        return item;
    }

    private static CBORTaggedItem requireEncodedDataItem(final CBORItem item) {
        require(item instanceof final CBORTaggedItem tagged && tagged.getTagNumber().intValue() == ENCODED_CBOR_DATA_ITEM
            && tagged.getTagContent() instanceof CBORByteArray, "Encoded CBOR data item is invalid");
        return (CBORTaggedItem) item;
    }

    private static CBORItem decodeEmbedded(final CBORItem item) throws IOException {
        return decode(((CBORByteArray) requireEncodedDataItem(item).getTagContent()).getValue());
    }

    private static CBORItem find(final CBORPairList map, final String key) {
        val pair = map.findByKey(key);
        require(pair != null, "CBOR map has no " + key);
        return pair.getValue();
    }

    private static @Nullable Object readValue(final CBORPairList map, final String key) {
        val pair = map.findByKey(key);
        return pair != null && pair.getValue() instanceof final CBORValue<?> value ? value.getValue() : null;
    }

    private static String keyOf(final CBORPair pair) {
        require(pair.getKey() instanceof CBORString, "CBOR map key is not text");
        return ((CBORString) pair.getKey()).getValue();
    }

    private static CBORPairList asMap(final CBORItem item, final String name) {
        require(item instanceof CBORPairList, name + " is not a CBOR map");
        return (CBORPairList) item;
    }

    private static List<? extends CBORItem> asList(final CBORItem item, final String name) {
        require(item instanceof CBORItemList, name + " is not a CBOR array");
        return ((CBORItemList) item).getItems();
    }

    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
