package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadata;
import com.authlete.cbor.CBORByteArray;
import com.authlete.cbor.CBORDecoder;
import com.authlete.cbor.CBORItemList;
import com.authlete.cbor.CBORPairList;
import com.authlete.cbor.CBORTaggedItem;
import com.authlete.cose.COSEKey;
import com.authlete.cose.COSESign1;
import com.authlete.cose.COSEVerifier;
import com.authlete.cose.constants.COSEAlgorithms;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.Base64URL;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OidcVerifiableCredentialMsoMdocTests}, for ISO/IEC 18013-5 mdoc credentials in the {@code mso_mdoc}
 * format of OpenID4VCI 1.0, appendix A.2.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks.file-system.jwks-file=classpath:vc-issuer-x5c.jwks",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.format=MSO_MDOC",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.doctype=org.iso.18013.5.1.mDL",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.credential-validity=P7D",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.cryptographic-binding-methods-supported=cose_key",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.claims.given_name.mandatory=true",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.claims.given_name.namespace=org.iso.18013.5.1",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.claims.family_name.namespace=org.iso.18013.5.1",
    "cas.authn.oidc.vc.issuer.credential-configurations.mdl.claims.student_id.mandatory=true"
})
class OidcVerifiableCredentialMsoMdocTests extends OidcVerifiableCredentialEndpointControllerTests.BaseTests {
    private static final String DOCTYPE = "org.iso.18013.5.1.mDL";

    private static final String NAMESPACE = "org.iso.18013.5.1";

    @Test
    void verifyMdocIsSignedOverItsClaimsAndBoundToTheDeviceKey() throws Throwable {
        val holderKey = generateEcHolderKey();
        val issuerSigned = (CBORPairList) new CBORDecoder(Base64URL.from(
            issueMdoc(buildProofJwt(holderKey, JWSAlgorithm.ES256, CREDENTIAL_ISSUER, new Date()))).decode()).next();

        val issuerKey = assertInstanceOf(ECKey.class, JWKSet.load(
            new ClassPathResource("vc-issuer-x5c.jwks").getInputStream()).getKeyByKeyId("vc-issuer"));
        val issuerAuth = COSESign1.build(issuerSigned.findByKey("issuerAuth").getValue());
        assertEquals(COSEAlgorithms.ES256, ((Number) issuerAuth.getProtectedHeader().getAlg()).intValue());
        val chain = issuerAuth.getUnprotectedHeader().getX5Chain();
        assertEquals(List.of(issuerKey.getParsedX509CertChain().getFirst()), chain);
        assertTrue(new COSEVerifier(chain.getFirst().getPublicKey()).verify(issuerAuth));

        val msoBytes = (CBORTaggedItem) new CBORDecoder(((CBORByteArray) issuerAuth.getPayload()).getValue()).next();
        assertEquals(24, msoBytes.getTagNumber().intValue());
        val mso = (Map<?, ?>) new CBORDecoder(((CBORByteArray) msoBytes.getTagContent()).getValue()).next().parse();
        assertEquals(DOCTYPE, mso.get("docType"));
        assertEquals("SHA-256", mso.get("digestAlgorithm"));

        val deviceKey = COSEKey.build((Map<Object, Object>) ((Map<?, ?>) mso.get("deviceKeyInfo")).get("deviceKey"));
        assertArrayEquals(holderKey.toECPublicKey().getEncoded(), deviceKey.createPublicKey().getEncoded());

        val validity = (Map<?, ?>) mso.get("validityInfo");
        val validFrom = Instant.parse(validity.get("validFrom").toString());
        assertEquals(Duration.ofDays(7), Duration.between(validFrom, Instant.parse(validity.get("validUntil").toString())));
        assertEquals(validFrom, Instant.parse(validity.get("signed").toString()));

        val digests = (Map<?, ?>) mso.get("valueDigests");
        assertEquals(Set.of(NAMESPACE, DOCTYPE), digests.keySet());
        val elements = new HashMap<String, Object>();
        val nameSpaces = (CBORPairList) issuerSigned.findByKey("nameSpaces").getValue();
        for (val namespace : List.of(NAMESPACE, DOCTYPE)) {
            for (val itemBytes : ((CBORItemList) nameSpaces.findByKey(namespace).getValue()).getItems()) {
                val tagged = (CBORTaggedItem) itemBytes;
                assertEquals(24, tagged.getTagNumber().intValue());
                val item = (Map<?, ?>) new CBORDecoder(((CBORByteArray) tagged.getTagContent()).getValue()).next().parse();
                assertTrue(((byte[]) item.get("random")).length >= 16);
                val digest = ((Map<?, ?>) digests.get(namespace)).entrySet()
                    .stream()
                    .filter(entry -> ((Number) entry.getKey()).intValue() == ((Number) item.get("digestID")).intValue())
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow();
                assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(tagged.encode()), (byte[]) digest);
                elements.put(namespace + '/' + item.get("elementIdentifier"), item.get("elementValue"));
            }
        }
        assertEquals(Map.of(NAMESPACE + "/given_name", "CAS", NAMESPACE + "/family_name", "User",
            DOCTYPE + "/student_id", "S12345"), elements);
    }

    @Test
    void verifyRsaDeviceKeyCannotBeBound() throws Throwable {
        performMdocRequest(buildValidRsaProofJwt())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(OidcConstants.VC_ERROR_INVALID_PROOF));
    }

    @Test
    void verifyMetadataDescribesTheMdoc() {
        val configuration = oidcCredentialIssuerMetadataService.build().getCredentialConfigurationsSupported().get("mdl");
        assertEquals(CredentialConfigurationFormats.MSO_MDOC.getValue(), configuration.getFormat());
        assertEquals(DOCTYPE, configuration.getDoctype());
        assertNull(configuration.getVct());
        assertNull(configuration.getCredentialDefinition());
        assertEquals(List.of("cose_key"), configuration.getCryptographicBindingMethodsSupported());
        assertEquals(List.of(COSEAlgorithms.ES256), configuration.getCredentialSigningAlgValuesSupported());
        assertEquals(List.of(List.of(NAMESPACE, "given_name"), List.of(NAMESPACE, "family_name"), List.of(DOCTYPE, "student_id")),
            configuration.getCredentialMetadata().getClaims().stream().map(OidcCredentialIssuerMetadata.ClaimMetadata::getPath).toList());
    }

    private String issueMdoc(final String proof) throws Throwable {
        val response = performMdocRequest(proof)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        val credentials = assertInstanceOf(List.class, MAPPER.readValue(response, Map.class).get("credentials"));
        return assertInstanceOf(Map.class, credentials.getFirst()).get("credential").toString();
    }

    private ResultActions performMdocRequest(final String proof) throws Throwable {
        val clientId = UUID.randomUUID().toString();
        servicesManager.save(getOidcRegisteredService(clientId));
        val request = new OidcVerifiableCredentialRequest();
        request.setCredentialConfigurationId("mdl");
        request.setProofs(buildProofs(proof));
        return mockMvc.perform(post(CREDENTIAL_ENDPOINT_URL)
            .with(withHttpRequestProcessor())
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + createOAuth20AccessToken(clientId).getId())
            .content(MAPPER.writeValueAsString(request)));
    }
}
