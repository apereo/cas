// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.yubico.core;

import org.apereo.cas.configuration.CasConfigurationProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.data.AssertionRequestWrapper;
import com.yubico.data.AssertionResponse;
import com.yubico.data.CredentialRegistration;
import com.yubico.data.RegistrationRequest;
import com.yubico.data.RegistrationResponse;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.AttestationMetadataSource;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

@Setter
@Slf4j
@RequiredArgsConstructor
public class WebAuthnServer {
    private static final int IDENTIFIER_LENGTH = 32;
    private static final ObjectMapper OBJECT_MAPPER = JacksonCodecs.json();

    private final RegistrationStorage userStorage;
    private final WebAuthnCache<RegistrationRequest> registerRequestStorage;
    private final WebAuthnCache<AssertionRequestWrapper> assertRequestStorage;
    private final RelyingParty relyingParty;
    private final SessionManager sessionManager;
    private final CasConfigurationProperties casProperties;

    public Either<String, RegistrationRequest> startRegistration(
        @NonNull final String username,
        final Optional<String> displayName,
        final Optional<String> credentialNickname,
        final ResidentKeyRequirement residentKeyRequirement,
        final Optional<ByteArray> sessionToken) throws ExecutionException {

        LOGGER.trace("Starting registration operation for username: [{}], credentialNickname: [{}]", username, credentialNickname);
        val registrations = userStorage.getRegistrationsByUsername(username);
        val existingUser = registrations.stream().findAny().map(CredentialRegistration::getUserIdentity);
        val permissionGranted = casProperties.getAuthn().getMfa().getWebAuthn().getCore().isMultipleDeviceRegistrationEnabled()
            || existingUser.map(userIdentity -> sessionManager.isSessionForUser(userIdentity.getId(), sessionToken)).orElse(true);

        if (permissionGranted) {
            val registrationUserId = existingUser.orElseGet(() ->
                UserIdentity.builder()
                    .name(username)
                    .displayName(displayName.orElseThrow())
                    .id(SessionManager.generateRandom(IDENTIFIER_LENGTH))
                    .build()
            );

            val request = new RegistrationRequest(
                username,
                credentialNickname,
                SessionManager.generateRandom(IDENTIFIER_LENGTH),
                relyingParty.startRegistration(
                    StartRegistrationOptions.builder()
                        .user(registrationUserId)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                            .residentKey(residentKeyRequirement)
                            .build()
                        )
                        .build()
                ),
                Optional.of(sessionManager.createSession(registrationUserId.getId()))
            );
            registerRequestStorage.put(request.requestId(), request);
            return Either.right(request);
        }
        return Either.left("The username %s is already registered and/or has an active session.".formatted(username));
    }

    public Either<List<String>, SuccessfulRegistrationResult> finishRegistration(final String responseJson) {
        LOGGER.trace("Finishing registration with response: [{}]", responseJson);
        RegistrationResponse response;
        try {
            response = OBJECT_MAPPER.readValue(responseJson, RegistrationResponse.class);
        } catch (final IOException e) {
            LOGGER.error("Registration failed; response: [{}]", responseJson, e);
            return Either.left(List.of("Registration failed", "Failed to decode response object.", e.getMessage()));
        }

        val request = registerRequestStorage.getIfPresent(response.requestId());
        registerRequestStorage.invalidate(response.requestId());

        if (request == null) {
            LOGGER.debug("Finishing registration failed with: [{}]", responseJson);
            return Either.left(List.of("Registration failed", "No such registration in progress."));
        } else {
            try {
                val registration = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                        .request(request.publicKeyCredentialCreationOptions())
                        .response(response.credential())
                        .build()
                );

                if (userStorage.userExists(request.username())) {
                    var permissionGranted = false;

                    val isValidSession = request.sessionToken().map(token ->
                        sessionManager.isSessionForUser(request.publicKeyCredentialCreationOptions().getUser().getId(), token)
                    ).orElse(false);

                    LOGGER.debug("Session token: [{}], valid session [{}]", request.sessionToken(), isValidSession);

                    if (isValidSession) {
                        permissionGranted = true;
                        LOGGER.info("Session token accepted for user [{}]", request.publicKeyCredentialCreationOptions().getUser().getId());
                    }

                    LOGGER.debug("Permission granted to finish registration: [{}]", permissionGranted);

                    if (!permissionGranted) {
                        throw new RegistrationFailedException(new IllegalArgumentException("User %s already exists".formatted(request.username())));
                    }
                }

                return Either.right(
                    new SuccessfulRegistrationResult(
                        request,
                        response,
                        addRegistration(
                            request.publicKeyCredentialCreationOptions().getUser(),
                            request.credentialNickname(),
                            registration
                        ),
                        registration.isAttestationTrusted() || relyingParty.isAllowUntrustedAttestation(),
                        sessionManager.createSession(request.publicKeyCredentialCreationOptions().getUser().getId())
                    )
                );
            } catch (final RegistrationFailedException e) {
                LOGGER.debug("Finishing registration failed with: [{}]", responseJson, e);
                return Either.left(List.of("Registration failed", e.getMessage()));
            } catch (final Exception e) {
                LOGGER.error("Finishing registration failed with: [{}]", responseJson, e);
                return Either.left(List.of("Registration failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(final Optional<String> username) {
        if (username.isPresent() && !userStorage.userExists(username.get())) {
            return Either.left(List.of("The username %s is not registered.".formatted(username.get())));
        }
        val request = new AssertionRequestWrapper(
            SessionManager.generateRandom(IDENTIFIER_LENGTH),
            relyingParty.startAssertion(StartAssertionOptions.builder().username(username).build())
        );
        assertRequestStorage.put(request.getRequestId(), request);
        return Either.right(request);
    }

    public Either<List<String>, SuccessfulAuthenticationResult> finishAuthentication(final String responseJson) {
        final AssertionResponse response;
        try {
            response = OBJECT_MAPPER.readValue(responseJson, AssertionResponse.class);
        } catch (final IOException e) {
            LOGGER.debug("Failed to decode response object", e);
            return Either.left(List.of("Assertion failed!", "Failed to decode response object.", e.getMessage()));
        }

        val request = assertRequestStorage.getIfPresent(response.requestId());
        assertRequestStorage.invalidate(response.requestId());

        if (request == null) {
            return Either.left(List.of("Assertion failed!", "No such assertion in progress."));
        } else {
            try {
                val assertionResult = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                        .request(request.getRequest())
                        .response(response.credential())
                        .build()
                );

                if (assertionResult.isSuccess()) {
                    try {
                        userStorage.updateSignatureCount(assertionResult);
                    } catch (final Exception e) {
                        LOGGER.error(
                            "Failed to update signature count for user \"{}\", credential \"{}\"",
                            assertionResult.getUsername(),
                            response.credential().getId(),
                            e
                        );
                    }

                    val session = sessionManager.createSession(assertionResult.getCredential().getUserHandle());
                    return Either.right(
                        new SuccessfulAuthenticationResult(
                            request,
                            response,
                            userStorage.getRegistrationsByUsername(assertionResult.getUsername()),
                            assertionResult.getUsername(),
                            session
                        )
                    );
                } else {
                    return Either.left(List.of("Assertion failed: Invalid assertion."));
                }
            } catch (final AssertionFailedException e) {
                LOGGER.warn("Assertion failed", e);
                return Either.left(List.of("Assertion failed", e.getMessage()));
            } catch (final Exception e) {
                LOGGER.error("Assertion failed", e);
                return Either.left(List.of("Assertion failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    @Value
    public static class SuccessfulRegistrationResult {
        boolean success = true;

        RegistrationRequest request;

        RegistrationResponse response;

        CredentialRegistration registration;

        boolean attestationTrusted;

        Optional<AttestationCertInfo> attestationCert;

        @JsonSerialize(using = AuthDataSerializer.class)
        AuthenticatorData authData;

        String username;

        ByteArray sessionToken;

        public SuccessfulRegistrationResult(final RegistrationRequest request, final RegistrationResponse response,
                                            final CredentialRegistration registration,
                                            final boolean attestationTrusted, final ByteArray sessionToken) {
            this.request = request;
            this.response = response;
            this.registration = registration;
            this.attestationTrusted = attestationTrusted;
            attestationCert = Optional.ofNullable(
                    response.credential().getResponse().getAttestation().getAttestationStatement().get("x5c")
                ).map(certs -> certs.get(0))
                .flatMap((final JsonNode certDer) -> {
                    try {
                        return Optional.of(new ByteArray(certDer.binaryValue()));
                    } catch (final IOException e) {
                        LOGGER.error("Failed to get binary value from x5c element: {}", certDer, e);
                        return Optional.empty();
                    }
                })
                .map(AttestationCertInfo::new);
            this.authData = response.credential().getResponse().getParsedAuthenticatorData();
            this.username = request.username();
            this.sessionToken = sessionToken;
        }

    }

    @Value
    public static class AttestationCertInfo {
        ByteArray der;

        String text;

        public AttestationCertInfo(final ByteArray certDer) {
            der = certDer;
            X509Certificate cert = null;
            try {
                cert = CertificateParser.parseDer(certDer.getBytes());
            } catch (final CertificateException e) {
                LOGGER.error("Failed to parse attestation certificate");
            }
            if (cert == null) {
                text = null;
            } else {
                text = cert.toString();
            }
        }
    }

    @Value
    @AllArgsConstructor
    public static class SuccessfulAuthenticationResult {
        boolean success = true;

        AssertionRequestWrapper request;

        AssertionResponse response;

        Collection<CredentialRegistration> registrations;

        @JsonSerialize(using = AuthDataSerializer.class)
        AuthenticatorData authData;

        String username;

        ByteArray sessionToken;

        public SuccessfulAuthenticationResult(final AssertionRequestWrapper request, final AssertionResponse response,
                                              final Collection<CredentialRegistration> registrations,
                                              final String username, final ByteArray sessionToken) {
            this(
                request,
                response,
                registrations,
                response.credential().getResponse().getParsedAuthenticatorData(),
                username,
                sessionToken
            );
        }
    }

    @Value
    public static class DeregisterCredentialResult {
        boolean success = true;

        CredentialRegistration droppedRegistration;

        boolean accountDeleted;
    }

    private static class AuthDataSerializer extends JsonSerializer<AuthenticatorData> {
        @Override
        public void serialize(final AuthenticatorData value, final JsonGenerator gen,
                              final SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("rpIdHash", value.getRpIdHash().getHex());
            gen.writeObjectField("flags", value.getFlags());
            gen.writeNumberField("signatureCounter", value.getSignatureCounter());
            value.getAttestedCredentialData().ifPresent(acd -> {
                try {
                    gen.writeObjectFieldStart("attestedCredentialData");
                    gen.writeStringField("aaguid", acd.getAaguid().getHex());
                    gen.writeStringField("credentialId", acd.getCredentialId().getHex());
                    gen.writeStringField("publicKey", acd.getCredentialPublicKey().getHex());
                    gen.writeEndObject();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gen.writeObjectField("extensions", value.getExtensions());
            gen.writeEndObject();
        }
    }

    private CredentialRegistration addRegistration(
        final UserIdentity userIdentity,
        final Optional<String> nickname,
        final RegistrationResult result) {

        return addRegistration(
            userIdentity,
            nickname,
            RegisteredCredential.builder()
                .credentialId(result.getKeyId().getId())
                .userHandle(userIdentity.getId())
                .publicKeyCose(result.getPublicKeyCose())
                .signatureCount(result.getSignatureCount())
                .build(),
            result.getKeyId().getTransports().orElseGet(TreeSet::new),
            result
                .getAttestationTrustPath()
                .flatMap(x5c -> x5c.stream().findFirst())
                .flatMap(cert -> {
                    if (relyingParty.getAttestationTrustSource().isPresent() &&
                        relyingParty.getAttestationTrustSource().get() instanceof final AttestationMetadataSource source) {
                        return source.findMetadata(cert);
                    }
                    return Optional.empty();
                }));
    }


    private CredentialRegistration addRegistration(
        final UserIdentity userIdentity,
        final Optional<String> nickname,
        final RegisteredCredential credential,
        final SortedSet<AuthenticatorTransport> transports,
        final Optional<Attestation> attestationMetadata) {
        val reg = CredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname.orElse(null))
            .registrationTime(Clock.systemUTC().instant())
            .credential(credential)
            .transports(transports)
            .attestationMetadata(attestationMetadata.orElse(null))
            .build();
        LOGGER.debug("Adding registration: user: [{}], nickname: [{}], credential: [{}]", userIdentity, nickname, credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

}
