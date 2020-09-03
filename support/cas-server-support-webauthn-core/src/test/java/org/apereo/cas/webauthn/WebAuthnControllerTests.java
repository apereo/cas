package org.apereo.cas.webauthn;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.yubico.util.Either;
import com.yubico.webauthn.core.WebAuthnServer;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RegistrationRequest;
import com.yubico.webauthn.data.RegistrationResponse;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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
public class WebAuthnControllerTests {

    @Test
    public void verifyRegistration() throws Exception {
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
        when(server.startRegistration(anyString(), any(), any(), anyBoolean(), any())).thenReturn(Either.right(registrationRequest));

        var result = controller.startRegistration("casuser", "displayName", "nickName", false, "sessionToken");
        assertEquals(HttpStatus.OK, result.getStatusCode());

        when(server.startRegistration(anyString(), any(), any(), anyBoolean(), any())).thenReturn(Either.left("failed"));
        result = controller.startRegistration("casuser", "displayName", "nickName", false, "sessionToken");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        when(server.finishRegistration(anyString())).thenReturn(Either.left(List.of("Fails")));
        result = controller.finishRegistration("registration-data");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        val publicKeyCredential = PublicKeyCredential.builder()
            .id(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
            .response(mock(AuthenticatorResponse.class))
            .clientExtensionResults(ClientRegistrationExtensionOutputs.builder().build())
            .build();
        val registrationResponse = new RegistrationResponse(
            ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)),
            (PublicKeyCredential) publicKeyCredential,
            Optional.of(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8))));
        when(server.finishRegistration(anyString())).thenReturn(
            Either.right(new WebAuthnServer.SuccessfulRegistrationResult(
                registrationRequest, registrationResponse,
                CredentialRegistration.builder().build(), true,
                ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))));
        result = controller.finishRegistration("registration-data");
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
