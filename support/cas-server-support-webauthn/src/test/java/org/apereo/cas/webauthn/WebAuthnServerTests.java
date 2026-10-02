package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.core.RegistrationStorage;
import com.yubico.core.SessionManager;
import com.yubico.core.WebAuthnCache;
import com.yubico.core.WebAuthnServer;
import com.yubico.data.AssertionRequestWrapper;
import com.yubico.data.CredentialRegistration;
import com.yubico.data.RegistrationRequest;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebAuthnServerTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class WebAuthnServerTests {
    private static final String FIDO_U2F_ATTESTATION_OBJECT =
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
            + "2ab969364ec6a6202200ea1acec627fd0e616d23da3e8bfa38a5527f2007cfe3fed63e5f3e2f7e25b11";

    @SuppressWarnings("unchecked")
    private static <T> WebAuthnCache<T> newWebAuthnCache() {
        return mock(WebAuthnCache.class);
    }

    private static WebAuthnServer newWebAuthnServer(final RegistrationStorage storage,
                                                    final WebAuthnCache<RegistrationRequest> registrationRequests,
                                                    final WebAuthnCache<AssertionRequestWrapper> assertionRequests,
                                                    final RelyingParty relyingParty) {
        val sessionManager = mock(SessionManager.class);
        when(sessionManager.createSession(any(), any())).thenReturn(SessionManager.generateRandom(32));
        return new WebAuthnServer(storage, registrationRequests, assertionRequests,
            relyingParty, sessionManager, new CasConfigurationProperties());
    }

    private static String toBase64Url(final String value) {
        return new ByteArray(value.getBytes(StandardCharsets.UTF_8)).getBase64Url();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void verifyRegistrationRecordsDiscoverableCredential(final boolean conditional) throws Throwable {
        val storage = mock(RegistrationStorage.class);
        val registrationRequests = WebAuthnServerTests.<RegistrationRequest>newWebAuthnCache();
        val requestId = SessionManager.generateRandom(16);
        val options = PublicKeyCredentialCreationOptions.builder()
            .rp(RelyingPartyIdentity.builder().id("localhost").name("CAS").build())
            .user(UserIdentity.builder().name(conditional ? "Typed@Example.org" : "casuser")
                .displayName("CAS").id(SessionManager.generateRandom(32)).build())
            .challenge(SessionManager.generateRandom(32))
            .pubKeyCredParams(List.of(PublicKeyCredentialParameters.ES256))
            .build();
        when(registrationRequests.getIfPresent(any(), eq(requestId))).thenReturn(new RegistrationRequest("casuser",
            conditional ? Optional.empty() : Optional.of("passkey"), requestId, options, Optional.empty(), conditional));

        val credentialId = SessionManager.generateRandom(16);
        val registrationResult = mock(RegistrationResult.class);
        when(registrationResult.getKeyId()).thenReturn(PublicKeyCredentialDescriptor.builder().id(credentialId).build());
        when(registrationResult.getPublicKeyCose()).thenReturn(SessionManager.generateRandom(77));
        when(registrationResult.getAttestationTrustPath()).thenReturn(Optional.empty());
        when(registrationResult.isDiscoverable()).thenReturn(Optional.of(Boolean.TRUE));
        when(registrationResult.isBackupEligible()).thenReturn(Boolean.TRUE);
        when(registrationResult.isBackedUp()).thenReturn(Boolean.TRUE);
        when(registrationResult.getAaguid()).thenReturn(ByteArray.fromHex("ea9b8d664d011d213ce4b6b48cb575d4"));
        val relyingParty = mock(RelyingParty.class);
        when(relyingParty.finishRegistration(any())).thenReturn(registrationResult);
        when(relyingParty.isAllowUntrustedAttestation()).thenReturn(Boolean.TRUE);

        val server = newWebAuthnServer(storage, registrationRequests, newWebAuthnCache(), relyingParty);
        val clientData = toBase64Url("""
            {"type":"webauthn.create","challenge":"%s","origin":"https://localhost:8443"}
            """.formatted(options.getChallenge().getBase64Url()));
        val responseJson = """
            {"requestId":"%1$s","sessionToken":null,"credential":{"id":"%2$s","rawId":"%2$s","type":"public-key",
            "authenticatorAttachment":"cross-platform","clientExtensionResults":{"credProps":{"rk":true}},
            "response":{"clientDataJSON":"%3$s","attestationObject":"%4$s","authenticatorData":"%2$s",
            "transports":["usb"],"publicKey":"%2$s","publicKeyAlgorithm":-7}}}
            """.formatted(requestId.getBase64Url(), credentialId.getBase64Url(), clientData,
            ByteArray.fromHex(FIDO_U2F_ATTESTATION_OBJECT).getBase64Url());

        val result = server.finishRegistration(new MockHttpServletRequest(), responseJson);
        assertTrue(result.isRight(), () -> String.join(",", result.left().orElseThrow()));
        assertTrue(result.right().orElseThrow().isAttestationTrusted());
        val passkeyProvider = result.right().orElseThrow().getPasskeyProvider();
        assertEquals("Google Password Manager", passkeyProvider.name());
        assertTrue(passkeyProvider.icon().startsWith("data:image/svg+xml;base64,"));
        verify(storage).addRegistrationByUsername(eq("casuser"),
            argThat((CredentialRegistration registration) -> Boolean.TRUE.equals(registration.getDiscoverable())
                && registration.getCredential().isBackupEligible().orElse(Boolean.FALSE)
                && registration.getCredential().isBackedUp().orElse(Boolean.FALSE)
                && "ea9b8d66-4d01-1d21-3ce4-b6b48cb575d4".equals(registration.getAaguid())
                && "casuser".equals(registration.getUserIdentity().getName())
                && (conditional ? "Google Password Manager" : "passkey").equals(registration.getCredentialNickname())));
        verify(relyingParty).finishRegistration(argThat((FinishRegistrationOptions finish) -> finish.isConditionalCreate() == conditional));
        assertTrue(WebAuthnUtils.toAaguid(new ByteArray(new byte[16])).isEmpty());
        assertTrue(server.startConditionalRegistration(new MockHttpServletRequest(), "casuser",
            Optional.of("Typed@Example.org"), Optional.of("CAS")).isLeft());
    }

    @Test
    void verifyResidentKeyRequirement() {
        val properties = new CasConfigurationProperties();
        val server = new WebAuthnServer(mock(RegistrationStorage.class), newWebAuthnCache(), newWebAuthnCache(),
            mock(RelyingParty.class), mock(SessionManager.class), properties);
        assertEquals(ResidentKeyRequirement.REQUIRED, server.determineResidentKeyRequirement(true));
        assertEquals(ResidentKeyRequirement.DISCOURAGED, server.determineResidentKeyRequirement(false));

        properties.getAuthn().getMfa().getWebAuthn().getCore().setAllowPrimaryAuthentication(true);
        assertEquals(ResidentKeyRequirement.REQUIRED, server.determineResidentKeyRequirement(true));
        assertEquals(ResidentKeyRequirement.PREFERRED, server.determineResidentKeyRequirement(false));
    }

    @Test
    void verifyFailedAssertionReportsUnknownCredential() throws Throwable {
        val storage = mock(RegistrationStorage.class);
        val assertionRequests = WebAuthnServerTests.<AssertionRequestWrapper>newWebAuthnCache();
        val options = PublicKeyCredentialRequestOptions.builder()
            .challenge(SessionManager.generateRandom(32))
            .rpId("localhost")
            .build();
        val withUsername = SessionManager.generateRandom(16);
        when(assertionRequests.getIfPresent(any(), eq(withUsername))).thenReturn(new AssertionRequestWrapper(withUsername,
            AssertionRequest.builder().publicKeyCredentialRequestOptions(options).username("casuser").build()));
        val withoutUsername = SessionManager.generateRandom(16);
        when(assertionRequests.getIfPresent(any(), eq(withoutUsername))).thenReturn(new AssertionRequestWrapper(withoutUsername,
            AssertionRequest.builder().publicKeyCredentialRequestOptions(options).build()));
        val relyingParty = mock(RelyingParty.class);
        when(relyingParty.finishAssertion(any())).thenThrow(new AssertionFailedException("Unknown credential"));
        val server = newWebAuthnServer(storage, newWebAuthnCache(), assertionRequests, relyingParty);

        val responseJson = """
            {"requestId":"%s","sessionToken":null,"credential":{"id":"ibE9wQddsF806g8uL9hDzgwLJipKhS9esD07Jmj0N98",
            "rawId":"ibE9wQddsF806g8uL9hDzgwLJipKhS9esD07Jmj0N98","type":"public-key","authenticatorAttachment":"platform",
            "clientExtensionResults":{},"response":{"authenticatorData":"SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2MBAAAFOQ",
            "clientDataJSON":"%s","signature":"-8AKZkFZSNUemUihJhsUp8LqXFHgVTjfCuKVvf1kbIkuwz5ClZK2u562C8rkUnIorxtzD7ujYh1z4FstXKyRDg"}}}
            """;
        val clientData = toBase64Url("""
            {"type":"webauthn.get","challenge":"%s","origin":"https://localhost:8443"}
            """.formatted(options.getChallenge().getBase64Url()));
        val request = new MockHttpServletRequest();

        assertTrue(server.finishAuthentication(request, responseJson.formatted(withUsername.getBase64Url(), clientData))
            .left().orElseThrow().unknownCredential());
        assertFalse(server.finishAuthentication(request, responseJson.formatted(withoutUsername.getBase64Url(), clientData))
            .left().orElseThrow().unknownCredential());

        when(storage.getRegistrationByUsernameAndCredentialId(eq("casuser"), any()))
            .thenReturn(Optional.of(CredentialRegistration.builder().build()));
        assertFalse(server.finishAuthentication(request, responseJson.formatted(withUsername.getBase64Url(), clientData))
            .left().orElseThrow().unknownCredential());
    }

    abstract static class BaseWebAuthnServerStartRegistrationTests {
        @Autowired
        @Qualifier("webAuthnServer")
        protected WebAuthnServer webAuthnServer;

        protected abstract AuthenticatorAttachment expectedAuthenticatorAttachment();

        protected abstract UserVerificationRequirement expectedUserVerificationRequirement();

        protected abstract ResidentKeyRequirement requestedResidentKeyRequirement();

        protected List<String> expectedHints() {
            return List.of();
        }

        @Test
        void verifyAuthenticationOperation() {
            val authentication = webAuthnServer.startAuthentication(new MockHttpServletRequest(), Optional.empty());

            assertTrue(authentication.isRight());
            val options = authentication.right().orElseThrow().getPublicKeyCredentialRequestOptions();
            assertEquals(expectedUserVerificationRequirement(), options.getUserVerification().orElse(null));
            assertEquals(expectedHints(), options.getHints());
        }

        @Test
        void verifyOperation() {
            val request = new MockHttpServletRequest();
            val username = UUID.randomUUID().toString();
            val registration = webAuthnServer.startRegistration(
                request,
                username,
                Optional.of("CAS User"),
                Optional.of("key"),
                requestedResidentKeyRequirement(),
                Optional.empty()
            );

            assertTrue(registration.isRight());
            val options = registration.right().orElseThrow().publicKeyCredentialCreationOptions();
            val authenticatorSelection = options.getAuthenticatorSelection().orElseThrow();
            assertEquals(expectedAuthenticatorAttachment(), authenticatorSelection.getAuthenticatorAttachment().orElse(null));
            assertEquals(expectedUserVerificationRequirement(), authenticatorSelection.getUserVerification().orElse(null));
            assertEquals(requestedResidentKeyRequirement(), authenticatorSelection.getResidentKey().orElseThrow());
            assertEquals(expectedHints(), options.getHints());
            assertTrue(options.getExtensions().getCredProps());
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = "cas.server.name=https://localhost:8443")
    class WebAuthnServerStartRegistrationNullTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return null;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return null;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.REQUIRED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=REQUIRED",
            "cas.authn.mfa.web-authn.core.hints=client-device,hybrid",
            "cas.authn.mfa.web-authn.core.passkey-upgrade-enabled=true",
            "cas.authn.mfa.web-authn.core.allow-primary-authentication=true",
            "cas.authn.mfa.web-authn.core.allow-untrusted-attestation=true"
        })
    class WebAuthnServerStartRegistrationPlatformRequiredTests extends BaseWebAuthnServerStartRegistrationTests {
        @Test
        void verifyConditionalRegistration() {
            val username = UUID.randomUUID().toString();
            val result = webAuthnServer.startConditionalRegistration(new MockHttpServletRequest(), username,
                Optional.of("Typed@Example.org"), Optional.of("CAS User"));
            assertTrue(result.isRight());
            val registration = result.right().orElseThrow();
            assertTrue(registration.conditional());
            assertEquals(username, registration.username());
            val options = registration.publicKeyCredentialCreationOptions();
            assertEquals("Typed@Example.org", options.getUser().getName());
            assertEquals(AttestationConveyancePreference.NONE, options.getAttestation());
            val selection = options.getAuthenticatorSelection().orElseThrow();
            assertEquals(ResidentKeyRequirement.REQUIRED, selection.getResidentKey().orElseThrow());
            assertEquals(UserVerificationRequirement.PREFERRED, selection.getUserVerification().orElseThrow());
            assertTrue(selection.getAuthenticatorAttachment().isEmpty());
        }

        @Override
        protected List<String> expectedHints() {
            return List.of("client-device", "hybrid");
        }

        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.REQUIRED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.REQUIRED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=CROSS_PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=DISCOURAGED"
        })
    class WebAuthnServerStartRegistrationCrossPlatformDiscouragedTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.CROSS_PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.DISCOURAGED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.DISCOURAGED;
        }
    }

    @Nested
    @SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
        properties = {
            "cas.server.name=https://localhost:8443",
            "cas.authn.mfa.web-authn.core.authenticator-attachment=PLATFORM",
            "cas.authn.mfa.web-authn.core.user-verification-requirement=PREFERRED"
        })
    class WebAuthnServerStartRegistrationPlatformPreferredTests extends BaseWebAuthnServerStartRegistrationTests {
        @Override
        protected AuthenticatorAttachment expectedAuthenticatorAttachment() {
            return AuthenticatorAttachment.PLATFORM;
        }

        @Override
        protected UserVerificationRequirement expectedUserVerificationRequirement() {
            return UserVerificationRequirement.PREFERRED;
        }

        @Override
        protected ResidentKeyRequirement requestedResidentKeyRequirement() {
            return ResidentKeyRequirement.DISCOURAGED;
        }
    }
}
