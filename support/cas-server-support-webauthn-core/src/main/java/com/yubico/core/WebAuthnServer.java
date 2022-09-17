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

import org.apereo.cas.util.RandomUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.cache.Cache;
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
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

@Setter
@Slf4j
public class WebAuthnServer {
    private static final SecureRandom random = RandomUtils.getNativeInstance();

    private final Clock clock = Clock.systemUTC();

    private final ObjectMapper jsonMapper = JacksonCodecs.json();

    private final RelyingParty rp;

    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage;

    private final Cache<ByteArray, RegistrationRequest> registerRequestStorage;

    private final RegistrationStorage userStorage;

    private final SessionManager sessions;

    public WebAuthnServer(final RegistrationStorage userStorage, final Cache<ByteArray, RegistrationRequest> registerRequestStorage,
                          final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage, final RelyingParty rpId,
                          final SessionManager sessionManager) {
        this.userStorage = userStorage;
        this.registerRequestStorage = registerRequestStorage;
        this.assertRequestStorage = assertRequestStorage;
        this.rp = rpId;
        this.sessions = sessionManager;
    }

    private static ByteArray generateRandom(final int length) {
        var bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

    public Either<String, RegistrationRequest> startRegistration(
        @NonNull
        final String username,
        final Optional<String> displayName,
        final Optional<String> credentialNickname,
        final ResidentKeyRequirement residentKeyRequirement,
        final Optional<ByteArray> sessionToken) throws ExecutionException {
        LOGGER.trace("startRegistration username: {}, credentialNickname: {}", username, credentialNickname);

        var registrations = userStorage.getRegistrationsByUsername(username);
        var existingUser = registrations.stream().findAny().map(CredentialRegistration::getUserIdentity);
        val permissionGranted = existingUser
            .map(userIdentity ->
                sessions.isSessionForUser(userIdentity.getId(), sessionToken))
            .orElse(true);

        if (permissionGranted) {
            var registrationUserId = existingUser.orElseGet(() ->
                UserIdentity.builder()
                    .name(username)
                    .displayName(displayName.get())
                    .id(generateRandom(32))
                    .build()
            );

            val request = new RegistrationRequest(
                username,
                credentialNickname,
                generateRandom(32),
                rp.startRegistration(
                    StartRegistrationOptions.builder()
                        .user(registrationUserId)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                            .residentKey(residentKeyRequirement)
                            .build()
                        )
                        .build()
                ),
                Optional.of(sessions.createSession(registrationUserId.getId()))
            );
            registerRequestStorage.put(request.requestId(), request);
            return Either.right(request);
        } else {
            return Either.left("The username \"" + username + "\" is already registered.");
        }
    }

    public Either<List<String>, SuccessfulRegistrationResult> finishRegistration(final String responseJson) {
        LOGGER.trace("finishRegistration responseJson: {}", responseJson);
        RegistrationResponse response = null;
        try {
            response = jsonMapper.readValue(responseJson, RegistrationResponse.class);
        } catch (final IOException e) {
            LOGGER.error("JSON error in finishRegistration; responseJson: {}", responseJson, e);
            return Either.left(Arrays.asList("Registration failed!", "Failed to decode response object.", e.getMessage()));
        }

        val request = registerRequestStorage.getIfPresent(response.requestId());
        registerRequestStorage.invalidate(response.requestId());

        if (request == null) {
            LOGGER.debug("fail finishRegistration responseJson: {}", responseJson);
            return Either.left(Arrays.asList("Registration failed!", "No such registration in progress."));
        } else {
            try {
                val registration = rp.finishRegistration(
                    FinishRegistrationOptions.builder()
                        .request(request.publicKeyCredentialCreationOptions())
                        .response(response.credential())
                        .build()
                );

                if (userStorage.userExists(request.username())) {
                    var permissionGranted = false;

                    val isValidSession = request.sessionToken().map(token ->
                        sessions.isSessionForUser(request.publicKeyCredentialCreationOptions().getUser().getId(), token)
                    ).orElse(false);

                    LOGGER.debug("Session token: {}", request.sessionToken());
                    LOGGER.debug("Valid session: {}", isValidSession);

                    if (isValidSession) {
                        permissionGranted = true;
                        LOGGER.info("Session token accepted for user {}", request.publicKeyCredentialCreationOptions().getUser().getId());
                    }

                    LOGGER.debug("permissionGranted: {}", permissionGranted);

                    if (!permissionGranted) {
                        throw new RegistrationFailedException(new IllegalArgumentException(String.format(
                            "User %s already exists",
                            request.username()
                        )));
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
                        registration.isAttestationTrusted(),
                        sessions.createSession(request.publicKeyCredentialCreationOptions().getUser().getId())
                    )
                );
            } catch (final RegistrationFailedException e) {
                LOGGER.debug("fail finishRegistration responseJson: {}", responseJson, e);
                return Either.left(Arrays.asList("Registration failed", e.getMessage()));
            } catch (final Exception e) {
                LOGGER.error("fail finishRegistration responseJson: {}", responseJson, e);
                return Either.left(Arrays.asList("Registration failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(final Optional<String> username) {
        LOGGER.trace("startAuthentication username: {}", username);

        if (username.isPresent() && !userStorage.userExists(username.get())) {
            return Either.left(Collections.singletonList("The username \"" + username.get() + "\" is not registered."));
        } else {
            var request = new AssertionRequestWrapper(
                generateRandom(32),
                rp.startAssertion(
                    StartAssertionOptions.builder()
                        .username(username)
                        .build()
                )
            );

            assertRequestStorage.put(request.getRequestId(), request);

            return Either.right(request);
        }
    }

    public Either<List<String>, SuccessfulAuthenticationResult> finishAuthentication(final String responseJson) {
        final AssertionResponse response;
        try {
            response = jsonMapper.readValue(responseJson, AssertionResponse.class);
        } catch (final IOException e) {
            LOGGER.debug("Failed to decode response object", e);
            return Either.left(Arrays.asList("Assertion failed!", "Failed to decode response object.", e.getMessage()));
        }

        val request = assertRequestStorage.getIfPresent(response.requestId());
        assertRequestStorage.invalidate(response.requestId());

        if (request == null) {
            return Either.left(Arrays.asList("Assertion failed!", "No such assertion in progress."));
        } else {
            try {
                val result = rp.finishAssertion(
                    FinishAssertionOptions.builder()
                        .request(request.getRequest())
                        .response(response.credential())
                        .build()
                );

                if (result.isSuccess()) {
                    try {
                        userStorage.updateSignatureCount(result);
                    } catch (final Exception e) {
                        LOGGER.error(
                            "Failed to update signature count for user \"{}\", credential \"{}\"",
                            result.getUsername(),
                            response.credential().getId(),
                            e
                        );
                    }

                    return Either.right(
                        new SuccessfulAuthenticationResult(
                            request,
                            response,
                            userStorage.getRegistrationsByUsername(result.getUsername()),
                            result.getUsername(),
                            sessions.createSession(result.getUserHandle())
                        )
                    );
                } else {
                    return Either.left(Collections.singletonList("Assertion failed: Invalid assertion."));
                }
            } catch (final AssertionFailedException e) {
                LOGGER.debug("Assertion failed", e);
                return Either.left(Arrays.asList("Assertion failed!", e.getMessage()));
            } catch (final Exception e) {
                LOGGER.error("Assertion failed", e);
                return Either.left(Arrays.asList("Assertion failed unexpectedly; this is likely a bug.", e.getMessage()));
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
                .flatMap((JsonNode certDer) -> {
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

        val source = (AttestationMetadataSource) rp.getAttestationTrustSource().get();
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
                .flatMap(source::findMetadata));
    }


    private CredentialRegistration addRegistration(
        UserIdentity userIdentity,
        Optional<String> nickname,
        RegisteredCredential credential,
        SortedSet<AuthenticatorTransport> transports,
        Optional<Attestation> attestationMetadata) {
        val reg = CredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname.orElse(null))
            .registrationTime(clock.instant())
            .credential(credential)
            .transports(transports)
            .attestationMetadata(attestationMetadata.orElse(null))
            .build();
        LOGGER.debug("Adding registration: user: {}, nickname: {}, credential: {}", userIdentity, nickname, credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

}
