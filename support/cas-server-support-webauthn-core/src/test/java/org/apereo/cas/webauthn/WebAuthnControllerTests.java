package org.apereo.cas.webauthn;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.webauthn.web.WebAuthnController;

import com.yubico.core.WebAuthnServer;
import com.yubico.data.AssertionRequestWrapper;
import com.yubico.data.AssertionResponse;
import com.yubico.data.CredentialRegistration;
import com.yubico.data.RegistrationRequest;
import com.yubico.data.RegistrationResponse;
import com.yubico.util.Either;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.TokenBindingStatus;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebAuthnControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFAProvider")
public class WebAuthnControllerTests {

    @Test
    public void verifyStartAuthentication() throws Exception {
        val server = mock(WebAuthnServer.class);
        val controller = new WebAuthnController(server);

        when(server.startAuthentication(any())).thenReturn(Either.left(List.of("failed")));
        var result = controller.startAuthentication("casuser");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        val publicKeyRequest = PublicKeyCredentialRequestOptions.builder()
            .challenge(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .rpId("localhost")
            .timeout(100)
            .build();

        val assertionRequest = AssertionRequest.builder()
            .publicKeyCredentialRequestOptions(publicKeyRequest)
            .username("casuser")
            .build();

        val assertion = new AssertionRequestWrapper(
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            assertionRequest);

        when(server.startAuthentication(any())).thenReturn(Either.right(assertion));
        result = controller.startAuthentication("casuser");
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void verifyFinishAuthentication() throws Exception {
        val authn = RegisteredServiceTestUtils.getAuthentication();

        val server = mock(WebAuthnServer.class);
        val controller = new WebAuthnController(server);

        when(server.finishAuthentication(any())).thenReturn(Either.left(List.of("fails")));
        var result = controller.finishAuthentication("casuser");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        val registration = CredentialRegistration.builder()
            .registrationTime(Instant.now(Clock.systemUTC()))
            .credential(RegisteredCredential.builder()
                .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .build())
            .userIdentity(UserIdentity.builder()
                .name("casuser")
                .displayName("CAS")
                .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .build())
            .build();

        val publicKeyRequest = PublicKeyCredentialRequestOptions.builder()
            .challenge(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .rpId("localhost")
            .timeout(100)
            .build();

        val assertionRequest = AssertionRequest.builder()
            .publicKeyCredentialRequestOptions(publicKeyRequest)
            .username("casuser")
            .build();

        val assertion = new AssertionRequestWrapper(
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            assertionRequest);

        val assertionJson = "{\"id\":\"ibE9wQddsF806g8uL9hDzgwLJipKhS9esD07Jmj0N98\","
            + "\"response\":{\"authenticatorData\":\"SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MBAAAFOQ\","
            + "\"clientDataJSON\":\"eyJjaGFsbGVuZ2UiOiJOM0xqSTJKNXlseVdlM0VENU9UNFhITFJxSHdtX0o0OF9EX2hvSk9GZjMwIiwib3JpZ2"
            +
            "luIjoiaHR0cHM6Ly9sb2NhbGhvc3QiLCJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwidG9rZW5CaW5kaW5nIjp7InN0YXR1cyI6InN1cHBvcnRlZCJ9LCJjbGllbnRFeHRlbnNpb25zIjp7fX0\","
            + "\"signature\":\"-8AKZkFZSNUemUihJhsUp8LqXFHgVTjfCuKVvf1kbIkuwz5ClZK2u562C8rkUnIorxtzD7ujYh1z4FstXKyRDg\"},"
            + "\"clientExtensionResults\":{},\"type\":\"public-key\"}";
        val publicKeyCredential = PublicKeyCredential.parseAssertionResponseJson(assertionJson);
        val response = new AssertionResponse(
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            publicKeyCredential);
        val authnResult = new WebAuthnServer.SuccessfulAuthenticationResult(assertion,
            response, List.of(registration),
            "casuser", ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)), List.of());

        when(server.finishAuthentication(any())).thenReturn(Either.right(authnResult));
        result = controller.finishAuthentication("casuser");
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void verifyStartRegistration() throws Exception {
        val server = mock(WebAuthnServer.class);
        val controller = new WebAuthnController(server);

        val publicKeyCredential = PublicKeyCredentialCreationOptions.builder()
            .rp(new RelyingPartyIdentity.RelyingPartyIdentityBuilder.MandatoryStages()
                .id(RandomUtils.randomAlphabetic(8))
                .name(RandomUtils.randomAlphabetic(8))
                .build())
            .user(new UserIdentity.UserIdentityBuilder.MandatoryStages()
                .name(RandomUtils.randomAlphabetic(8))
                .displayName(RandomUtils.randomAlphabetic(8))
                .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .build())
            .challenge(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .pubKeyCredParams(List.of())
            .build();
        val registrationRequest = new RegistrationRequest("casuser", Optional.empty(),
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            publicKeyCredential,
            Optional.of(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8))));
        when(server.startRegistration(anyString(), any(), any(), anyBoolean(), any()))
            .thenReturn(Either.right(registrationRequest));

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        var result = controller.startRegistration("casuser", "displayName", "nickName", false, "sessionToken", request, response);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        when(server.startRegistration(anyString(), any(), any(), anyBoolean(), any())).thenReturn(Either.left("failed"));
        result = controller.startRegistration("casuser", "displayName", "nickName", false, "sessionToken", request, response);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void verifyFinishRegistration() throws Exception {
        val authn = RegisteredServiceTestUtils.getAuthentication();

        val server = mock(WebAuthnServer.class);
        val controller = new WebAuthnController(server);

        val registrationRequest = new RegistrationRequest("casuser", Optional.empty(),
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            new PublicKeyCredentialCreationOptions.PublicKeyCredentialCreationOptionsBuilder.MandatoryStages()
                .rp(new RelyingPartyIdentity.RelyingPartyIdentityBuilder.MandatoryStages()
                    .id(RandomUtils.randomAlphabetic(8)).name(RandomUtils.randomAlphabetic(8))
                    .build())
                .user(new UserIdentity.UserIdentityBuilder.MandatoryStages()
                    .name(RandomUtils.randomAlphabetic(8))
                    .displayName(RandomUtils.randomAlphabetic(8))
                    .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .challenge(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                .pubKeyCredParams(List.of())
                .build(),
            Optional.of(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8))));


        when(server.finishRegistration(anyString())).thenReturn(Either.left(List.of("Fails")));
        var result = controller.finishRegistration("registration-data");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        val exampleAttestation = ByteArray.fromHex(
            "a368617574684461746159012c49960de5880e8c687434170f6476605b8fe4aeb9a28632c7"
                + "995cf3ba831d976341000000000000000000000000000000000000000000a20008dce8bdc"
                + "3fc2c734a29a20ddb6509bceb721d7381859ab2548ae350fdb1962df68f1ebc08dbb5263c653b4"
                + "e855b45b7df85b4926ed4572f2af78da28028143d6a6de8c0afcc6c6fbb648ce0bac022ba0a2"
                + "303d2fced0d9772fcc0d32e281c8563082820e9bfd2e76241637ccbc36aebd85f398f6b6863d3d6755e3"
                + "98e05faf101e467c201219a83b2bf4269efc6e82f2c95dbfbc2a979ea2b78dea9b9fe467a2fa36361"
                + "6c6765455332353661785820c5df3292ce78ea68322b36073fd3b012a35cc9352cba7abd5ed2c287f6"
                + "112b5361795820a83b6a518319bee86dccd1c8d54b3acb4f590e2cf7d26616aad3e7aa49fc8b4c6366"
                + "6d74686669646f2d7532666761747453746d74a26378356381590136308201323081d9a0030201020"
                + "20500a5427a1d300a06082a8648ce3d0403023021311f301d0603550403131646697265666f782055"
                + "324620536f667420546f6b656e301e170d3137303833303134353130365a170d31373039303131343531"
                + "30365a3021311f301d0603550403131646697265666f782055324620536f667420546f6b656e30593013"
                + "06072a8648ce3d020106082a8648ce3d0301070342000409b9c8303e3a9f1cc0c4bb83c6d56a223699"
                + "137387ad27dd01ad9c8e0c80addce10e52e622197576f756e38d5965bf98d53ece5af4b0ec003ad08f932"
                + "bd84c1e300a06082a8648ce3d040302034800304502210083239a57e0fa99224b2c7989998cf833d5c1562"
                + "df38d285d46cab1d6cf46ae9e02204cfd5deb11de1fdafc4e899f8d03388164beaff2e4263a82210cc"
                + "c38906981236373696758463044022049c439848ec81672461cc0ea629f297cc7228450a6b0d0887"
                + "2ab969364ec6a6202200ea1acec627fd0e616d23da3e8bfa38a5527f2007cfe3fed63e5f3e2f7e25b11");

        val tokenBindingStatus = TokenBindingStatus.PRESENT;
        val tokenBindingId = ByteArray.fromBase64Url("IgqNmDkOp68Edjd8-uwxmh");
        val challenge = ByteArray.fromBase64Url("HfpNmDkOp66Edjd5-uvwlg");
        val clientJson = '{'
            + "\"authenticatorExtensions\":{\"boo\":\"far\"},"
            + "\"challenge\":\"" + challenge.getBase64Url() + "\","
            + "\"origin\":\"localhost\","
            + "\"tokenBinding\":{\"status\":\"" + tokenBindingStatus.toJsonString()
            + "\",\"id\":\"" + tokenBindingId.getBase64Url() + "\"},"
            + "\"type\":\"webauthn.get\""
            + '}';
        val response = AuthenticatorAttestationResponse.builder()
            .attestationObject(exampleAttestation)
            .clientDataJSON(new ByteArray(clientJson.getBytes(StandardCharsets.UTF_8)))
            .build();

        val publicKeyCredential = PublicKeyCredential.builder()
            .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .response(response)
            .clientExtensionResults(ClientRegistrationExtensionOutputs.builder().build())
            .build();
        val registrationResponse = new RegistrationResponse(
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            (PublicKeyCredential) publicKeyCredential,
            Optional.of(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8))));

        when(server.finishRegistration(anyString())).thenReturn(
            Either.right(new WebAuthnServer.SuccessfulRegistrationResult(
                registrationRequest, registrationResponse,
                CredentialRegistration.builder()
                    .registrationTime(Instant.now(Clock.systemUTC()))
                    .credential(RegisteredCredential.builder()
                        .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                        .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                        .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                        .build())
                    .userIdentity(UserIdentity.builder()
                        .name("casuser")
                        .displayName("CAS")
                        .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                        .build())
                    .build(), true,
                ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))));
        result = controller.finishRegistration("registration-data");
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
