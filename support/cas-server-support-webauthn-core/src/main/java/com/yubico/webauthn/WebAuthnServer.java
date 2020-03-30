package com.yubico.webauthn;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AssertionRequestWrapper;
import com.yubico.webauthn.data.AssertionResponse;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.RegistrationRequest;
import com.yubico.webauthn.data.RegistrationResponse;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.benmanes.caffeine.cache.Cache;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * This is {@link WebAuthnServer}.
 *
 * @author Misagh Moayyed
 * @since 6.2
 */
@Slf4j
@RequiredArgsConstructor
public class WebAuthnServer {

    private final RegistrationStorage userStorage;

    private final Cache<ByteArray, RegistrationRequest> registerRequestStorage;

    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage;

    private final RelyingParty relyingParty;

    private final SessionManager sessions = new SessionManager();

    private final Clock clock = Clock.system(ZoneOffset.UTC);

    private final ObjectMapper jsonMapper = JacksonCodecs.json().findAndRegisterModules();

    public Either<String, RegistrationRequest> startRegistration(
        @NonNull final String username,
        final Optional<String> displayName,
        final Optional<String> credentialNickname,
        final boolean requireResidentKey,
        final Optional<ByteArray> sessionToken) throws ExecutionException {


        val registrations = userStorage.getRegistrationsByUsername(username);
        val existingUser = registrations.stream().findAny().map(CredentialRegistration::getUserIdentity);
        val permissionGranted = existingUser
            .map(userIdentity -> sessions.isSessionForUser(userIdentity.getId(), sessionToken))
            .orElse(true);

        if (permissionGranted) {
            val registrationUserId = existingUser.orElseGet(() ->
                UserIdentity.builder()
                    .name(username)
                    .displayName(displayName.get())
                    .id(WebAuthnUtils.generateRandomId())
                    .build());

            val request = new RegistrationRequest(
                username,
                credentialNickname,
                WebAuthnUtils.generateRandomId(),
                relyingParty.startRegistration(
                    StartRegistrationOptions.builder()
                        .user(registrationUserId)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                            .requireResidentKey(requireResidentKey)
                            .build()
                        )
                        .build()
                ),
                Optional.of(sessions.createSession(registrationUserId.getId()))
            );
            registerRequestStorage.put(request.getRequestId(), request);
            return Either.right(request);
        }
        return Either.left("The username " + username + " is registered.");
    }

    public Either<List<String>, SuccessfulRegistrationResult> finishRegistration(final String responseJson) {
        LOGGER.trace("Finished registration with response: [{}]", responseJson);
        RegistrationResponse response = null;
        try {
            response = jsonMapper.readValue(responseJson, RegistrationResponse.class);
        } catch (final IOException e) {
            LOGGER.error("JSON error in registration; Response: [{}]", responseJson, e);
            return Either.left(Arrays.asList("Registration failed", "Failed to decode response object", e.getMessage()));
        }

        val request = registerRequestStorage.getIfPresent(response.getRequestId());
        registerRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            LOGGER.debug("Failed to finish registration: [{}]", responseJson);
            return Either.left(Arrays.asList("Registration failed!", "No such registration in progress."));
        }
        try {
            val registration = relyingParty.finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request.getPublicKeyCredentialCreationOptions())
                    .response(response.getCredential())
                    .build()
            );

            if (userStorage.userExists(request.getUsername())) {
                var permissionGranted = false;

                val isValidSession = request.getSessionToken().map(token ->
                    sessions.isSessionForUser(request.getPublicKeyCredentialCreationOptions().getUser().getId(), token)
                ).orElse(false);

                LOGGER.debug("Session token: [{}], validity [{}]", request.getSessionToken(), isValidSession);

                if (isValidSession) {
                    permissionGranted = true;
                    LOGGER.info("Session token accepted for user [{}]", request.getPublicKeyCredentialCreationOptions().getUser().getId());
                }

                LOGGER.debug("Permissions granted: [{}]", permissionGranted);

                if (!permissionGranted) {
                    throw new RegistrationFailedException(new IllegalArgumentException(String.format(
                        "User %s already exists",
                        request.getUsername()
                    )));
                }
            }

            return Either.right(
                new SuccessfulRegistrationResult(
                    request,
                    response,
                    addRegistration(
                        request.getPublicKeyCredentialCreationOptions().getUser(),
                        request.getCredentialNickname(),
                        response,
                        registration
                    ),
                    registration.isAttestationTrusted(),
                    sessions.createSession(request.getPublicKeyCredentialCreationOptions().getUser().getId())
                )
            );
        } catch (final Exception e) {
            LOGGER.debug("Failed to finish registration: [{}]", responseJson);
            LOGGER.trace(e.getMessage(), e);
            return Either.left(Arrays.asList("Registration failed.", e.getMessage()));
        }
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(final Optional<String> username) {
        if (username.isPresent() && !userStorage.userExists(username.get())) {
            return Either.left(Collections.singletonList("The username " + username.get() + " is not registered."));
        }
        val request = new AssertionRequestWrapper(
            WebAuthnUtils.generateRandomId(),
            relyingParty.startAssertion(
                StartAssertionOptions.builder()
                    .username(username)
                    .build()
            )
        );
        assertRequestStorage.put(request.getRequestId(), request);
        return Either.right(request);
    }

    public Either<List<String>, SuccessfulAuthenticationResult> finishAuthentication(final String responseJson) {

        var response = (AssertionResponse) null;
        try {
            response = jsonMapper.readValue(responseJson, AssertionResponse.class);
        } catch (final IOException e) {
            LOGGER.debug("Failed to decode response object", e);
            return Either.left(Arrays.asList("Assertion failed!", "Failed to decode response object.", e.getMessage()));
        }

        val request = assertRequestStorage.getIfPresent(response.getRequestId());
        assertRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            return Either.left(Arrays.asList("Assertion failed!", "No such assertion in progress."));
        }

        try {
            val result = relyingParty.finishAssertion(
                FinishAssertionOptions.builder()
                    .request(request.getRequest())
                    .response(response.getCredential())
                    .build()
            );

            if (result.isSuccess()) {
                try {
                    userStorage.updateSignatureCount(result);
                } catch (final Exception e) {
                    LOGGER.error(
                        "Failed to update signature count for user [{}}], credential [{}]",
                        result.getUsername(),
                        response.getCredential().getId(),
                        e
                    );
                }

                return Either.right(
                    new SuccessfulAuthenticationResult(
                        request,
                        response,
                        userStorage.getRegistrationsByUsername(result.getUsername()),
                        result.getUsername(),
                        sessions.createSession(result.getUserHandle()),
                        result.getWarnings()
                    )
                );
            }
            return Either.left(Collections.singletonList("Assertion failed: Invalid assertion."));
        } catch (final AssertionFailedException e) {
            LOGGER.debug("Assertion failed", e);
            return Either.left(Arrays.asList("Assertion failed!", e.getMessage()));
        } catch (final Exception e) {
            LOGGER.error("Assertion failed", e);
            return Either.left(Arrays.asList("Assertion failed unexpectedly.", e.getMessage()));
        }
    }

    public Either<List<String>, DeregisterCredentialResult> deregisterCredential(@NonNull final ByteArray sessionToken, final ByteArray credentialId) {

        if (credentialId == null || credentialId.getBytes().length == 0) {
            return Either.left(Collections.singletonList("Credential ID must not be empty."));
        }

        val session = sessions.getSession(sessionToken);
        if (session.isPresent()) {
            val userHandle = session.get();
            val username = userStorage.getUsernameForUserHandle(userHandle);

            if (username.isPresent()) {
                val credReg = userStorage.getRegistrationByUsernameAndCredentialId(username.get(), credentialId);
                if (credReg.isPresent()) {
                    userStorage.removeRegistrationByUsername(username.get(), credReg.get());

                    return Either.right(new DeregisterCredentialResult(
                        credReg.get(),
                        !userStorage.userExists(username.get())
                    ));
                }
                return Either.left(Collections.singletonList("Credential ID not registered:" + credentialId));
            }
            return Either.left(Collections.singletonList("Invalid user handle"));
        }
        return Either.left(Collections.singletonList("Invalid session"));
    }

    public <T> Either<List<String>, T> deleteAccount(final String username, final Supplier<T> onSuccess) {
        if (username == null || username.isEmpty()) {
            return Either.left(Collections.singletonList("Username must not be empty."));
        }

        val removed = userStorage.removeAllRegistrations(username);

        if (removed) {
            return Either.right(onSuccess.get());
        }
        return Either.left(Collections.singletonList("Username not registered:" + username));
    }

    private CredentialRegistration addRegistration(
        final UserIdentity userIdentity,
        final Optional<String> nickname,
        final RegistrationResponse response,
        final RegistrationResult result) {

        return addRegistration(
            userIdentity,
            nickname,
            response.getCredential().getResponse().getAttestation().getAuthenticatorData().getSignatureCounter(),
            RegisteredCredential.builder()
                .credentialId(result.getKeyId().getId())
                .userHandle(userIdentity.getId())
                .publicKeyCose(result.getPublicKeyCose())
                .signatureCount(response.getCredential().getResponse().getParsedAuthenticatorData().getSignatureCounter())
                .build(),
            result.getAttestationMetadata()
        );
    }

    private CredentialRegistration addRegistration(
        final UserIdentity userIdentity,
        final Optional<String> nickname,
        final long signatureCount,
        final RegisteredCredential credential,
        final Optional<Attestation> attestationMetadata) {
        val reg = CredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname)
            .registrationTime(clock.instant())
            .credential(credential)
            .signatureCount(signatureCount)
            .attestationMetadata(attestationMetadata)
            .build();

        LOGGER.debug(
            "Adding registration: user: [{}], nickname: [{}], credential: [{}]",
            userIdentity,
            nickname,
            credential
        );
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

    @Value
    public static class SuccessfulRegistrationResult {
        private boolean success = true;

        private RegistrationRequest request;

        private RegistrationResponse response;

        private CredentialRegistration registration;

        private boolean attestationTrusted;

        private Optional<AttestationCertInfo> attestationCert;

        @JsonSerialize(using = AuthDataSerializer.class)
        private AuthenticatorData authData;

        private String username;

        private ByteArray sessionToken;

        SuccessfulRegistrationResult(final RegistrationRequest request, final RegistrationResponse response,
                                     final CredentialRegistration registration, final boolean attestationTrusted,
                                     final ByteArray sessionToken) {
            this.request = request;
            this.response = response;
            this.registration = registration;
            this.attestationTrusted = attestationTrusted;
            attestationCert = Optional.ofNullable(response.getCredential().getResponse().getAttestation().getAttestationStatement().get("x5c")
            ).map(certs -> certs.get(0))
                .flatMap((JsonNode certDer) -> {
                    try {
                        return Optional.of(new ByteArray(certDer.binaryValue()));
                    } catch (final IOException e) {
                        LOGGER.error("Failed to get binary value from x5c element: [{}]", certDer, e);
                        return Optional.empty();
                    }
                })
                .map(AttestationCertInfo::new);
            this.authData = response.getCredential().getResponse().getParsedAuthenticatorData();
            this.username = request.getUsername();
            this.sessionToken = sessionToken;
        }

    }

    @Value
    private static class AttestationCertInfo {
        private ByteArray der;

        private String text;

        AttestationCertInfo(final ByteArray certDer) {
            der = certDer;

            X509Certificate cert = null;
            try {
                cert = CertificateParser.parseDer(certDer.getBytes());
            } catch (final Exception e) {
                LOGGER.error("Failed to parse attestation certificate", e);
            }
            if (cert != null) {
                text = cert.toString();
            } else {
                text = null;
            }
        }
    }

    @Value
    @AllArgsConstructor
    public static class SuccessfulAuthenticationResult {
        private boolean success = true;

        private AssertionRequestWrapper request;

        private AssertionResponse response;

        private Collection<CredentialRegistration> registrations;

        @JsonSerialize(using = AuthDataSerializer.class)
        private AuthenticatorData authData;

        private String username;

        private ByteArray sessionToken;

        private List<String> warnings;

        SuccessfulAuthenticationResult(final AssertionRequestWrapper request, final AssertionResponse response,
                                       final Collection<CredentialRegistration> registrations, final String username,
                                       final ByteArray sessionToken, final List<String> warnings) {
            this(
                request,
                response,
                registrations,
                response.getCredential().getResponse().getParsedAuthenticatorData(),
                username,
                sessionToken,
                warnings
            );
        }
    }

    @Value
    public static class DeregisterCredentialResult {
        private boolean success = true;

        private CredentialRegistration droppedRegistration;

        private boolean accountDeleted;
    }

    private static class AuthDataSerializer extends JsonSerializer<AuthenticatorData> {
        @Override
        public void serialize(final AuthenticatorData value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
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
}
