package com.yubico.core;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.core.SessionManager;
import com.yubico.core.WebAuthnCache;
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
import com.yubico.webauthn.data.AuthenticatorAttachment;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
    @Getter
    private final SessionManager sessionManager;
    private final CasConfigurationProperties casProperties;

    /**
     * Resident key requirement for a registration. A discoverable credential is required when the user asks for one;
     * otherwise one is preferred when WebAuthn may be used for primary authentication, so that the credential can also
     * serve as a passkey, and discouraged when WebAuthn is only a second factor, which keeps resident key slots on
     * security keys free.
     *
     * @param discoverableCredentialRequested whether the user asked for a discoverable credential
     * @return the resident key requirement
     */
    public ResidentKeyRequirement determineResidentKeyRequirement(final boolean discoverableCredentialRequested) {
        if (discoverableCredentialRequested) {
            return ResidentKeyRequirement.REQUIRED;
        }
        return casProperties.getAuthn().getMfa().getWebAuthn().getCore().isAllowPrimaryAuthentication()
            ? ResidentKeyRequirement.PREFERRED
            : ResidentKeyRequirement.DISCOURAGED;
    }

    public Either<String, RegistrationRequest> startRegistration(
        final HttpServletRequest request,
        @NonNull final String username,
        final Optional<String> displayName,
        final Optional<String> credentialNickname,
        final ResidentKeyRequirement residentKeyRequirement,
        final Optional<ByteArray> sessionToken) {

        LOGGER.trace("Starting registration operation for username: [{}], credentialNickname: [{}]", username, credentialNickname);
        val registrations = userStorage.getRegistrationsByUsername(username);
        val existingUser = registrations.stream().findAny().map(CredentialRegistration::getUserIdentity);
        val properties = casProperties.getAuthn().getMfa().getWebAuthn();
        val permissionGranted = properties.getCore().isMultipleDeviceRegistrationEnabled()
            || existingUser.map(userIdentity -> sessionManager.isSessionForUser(request, userIdentity.getId(), sessionToken)).orElse(true);

        if (permissionGranted) {
            val registrationUserId = existingUser.orElseGet(() ->
                UserIdentity.builder()
                    .name(username)
                    .displayName(displayName.orElseThrow())
                    .id(SessionManager.generateRandom(IDENTIFIER_LENGTH))
                    .build()
            );

            val authenticatorAttachementProperty = properties.getCore().getAuthenticatorAttachment();
            val authenticatorAttachement = StringUtils.isNotBlank(authenticatorAttachementProperty)
                ? AuthenticatorAttachment.valueOf(authenticatorAttachementProperty.toUpperCase(Locale.ROOT))
                : null;
            val userVerificationRequirementProperty = properties.getCore().getUserVerificationRequirement();
            val userVerificationRequirement = StringUtils.isNotBlank(userVerificationRequirementProperty)
                ? UserVerificationRequirement.valueOf(userVerificationRequirementProperty.toUpperCase(Locale.ROOT))
                : null;
            val registrationRequest = new RegistrationRequest(
                username,
                credentialNickname,
                SessionManager.generateRandom(IDENTIFIER_LENGTH),
                relyingParty.startRegistration(
                    StartRegistrationOptions.builder()
                        .user(registrationUserId)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                            .authenticatorAttachment(authenticatorAttachement)
                            .userVerification(userVerificationRequirement)
                            .residentKey(residentKeyRequirement)
                            .build()
                        )
                        .hints(properties.getCore().getHints())
                        .build()
                ),
                Optional.of(sessionManager.createSession(request, registrationUserId.getId()))
            );
            registerRequestStorage.put(request, registrationRequest.requestId(), registrationRequest);
            return Either.right(registrationRequest);
        }
        return Either.left("The username %s is already registered and/or has an active session.".formatted(username));
    }

    public Either<List<String>, SuccessfulRegistrationResult> finishRegistration(final HttpServletRequest request, final String responseJson) {
        LOGGER.trace("Finishing registration with response: [{}]", responseJson);
        RegistrationResponse registrationResponse;
        try {
            registrationResponse = OBJECT_MAPPER.readValue(responseJson, RegistrationResponse.class);
        } catch (final Exception e) {
            LOGGER.error("Registration failed; response: [{}]", responseJson, e);
            return Either.left(List.of("Registration failed", "Failed to decode response object.", e.getMessage()));
        }

        val registrationRequest = registerRequestStorage.getIfPresent(request, registrationResponse.requestId());
        registerRequestStorage.invalidate(request, registrationResponse.requestId());

        if (registrationRequest == null) {
            LOGGER.debug("Finishing registration failed with: [{}]", responseJson);
            return Either.left(List.of("Registration failed", "No such registration in progress."));
        } else {
            try {
                val registration = relyingParty.finishRegistration(
                    FinishRegistrationOptions.builder()
                        .request(registrationRequest.publicKeyCredentialCreationOptions())
                        .response(registrationResponse.credential())
                        .build()
                );

                if (userStorage.userExists(registrationRequest.username())) {
                    var permissionGranted = false;

                    val isValidSession = registrationRequest.sessionToken().map(token ->
                        sessionManager.isSessionForUser(request, registrationRequest.publicKeyCredentialCreationOptions().getUser().getId(), token)
                    ).orElse(false);

                    LOGGER.debug("Session token: [{}], valid session [{}]", registrationRequest.sessionToken(), isValidSession);

                    if (isValidSession) {
                        permissionGranted = true;
                        LOGGER.info("Session token accepted for user [{}]", registrationRequest.publicKeyCredentialCreationOptions().getUser().getId());
                    }

                    LOGGER.debug("Permission granted to finish registration: [{}]", permissionGranted);

                    if (!permissionGranted) {
                        throw new RegistrationFailedException(new IllegalArgumentException("User %s already exists".formatted(registrationRequest.username())));
                    }
                }

                return Either.right(
                    new SuccessfulRegistrationResult(
                        registrationRequest,
                        registrationResponse,
                        addRegistration(
                            registrationRequest.publicKeyCredentialCreationOptions().getUser(),
                            registrationRequest.credentialNickname(),
                            registration
                        ),
                        registration.isAttestationTrusted() || relyingParty.isAllowUntrustedAttestation(),
                        sessionManager.createSession(request, registrationRequest.publicKeyCredentialCreationOptions().getUser().getId())
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

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(final HttpServletRequest request, final Optional<String> username) {
        if (username.isPresent() && !userStorage.userExists(username.get())) {
            return Either.left(List.of("The username %s is not registered.".formatted(username.get())));
        }
        val core = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
        val userVerificationRequirementProperty = core.getUserVerificationRequirement();
        val userVerificationRequirement = StringUtils.isNotBlank(userVerificationRequirementProperty)
            ? UserVerificationRequirement.valueOf(userVerificationRequirementProperty.toUpperCase(Locale.ROOT))
            : null;
        val assertionRequest = new AssertionRequestWrapper(
            SessionManager.generateRandom(IDENTIFIER_LENGTH),
            relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(username)
                .userVerification(userVerificationRequirement)
                .hints(core.getHints())
                .build())
        );
        assertRequestStorage.put(request, assertionRequest.getRequestId(), assertionRequest);
        return Either.right(assertionRequest);
    }

    /**
     * Finish authentication.
     * <p>A failed assertion is reported as an unknown credential when the account that owns it, identified by the
     * user handle in the assertion or else by the username of the request, no longer holds the credential, so the
     * browser can stop offering it through the WebAuthn Signal API.</p>
     *
     * @param request      the request
     * @param responseJson the response json
     * @return the authentication result, or the failure
     */
    public Either<AuthenticationFailure, SuccessfulAuthenticationResult> finishAuthentication(final HttpServletRequest request, final String responseJson) {
        final AssertionResponse assertionResponse;
        try {
            assertionResponse = OBJECT_MAPPER.readValue(responseJson, AssertionResponse.class);
        } catch (final Exception e) {
            LOGGER.debug("Failed to decode response object", e);
            return Either.left(AuthenticationFailure.of("Assertion failed!", "Failed to decode response object.", e.getMessage()));
        }

        val assertionRequestWrapper = assertRequestStorage.getIfPresent(request, assertionResponse.requestId());
        assertRequestStorage.invalidate(request, assertionResponse.requestId());

        if (assertionRequestWrapper == null) {
            return Either.left(AuthenticationFailure.of("Assertion failed!", "No such assertion in progress."));
        } else {
            try {
                val assertionResult = relyingParty.finishAssertion(
                    FinishAssertionOptions.builder()
                        .request(assertionRequestWrapper.getRequest())
                        .response(assertionResponse.credential())
                        .build()
                );

                if (assertionResult.isSuccess()) {
                    try {
                        userStorage.updateSignatureCount(assertionResult);
                    } catch (final Exception e) {
                        LOGGER.error(
                            "Failed to update signature count for user \"{}\", credential \"{}\"",
                            assertionResult.getUsername(),
                            assertionResponse.credential().getId(),
                            e
                        );
                    }

                    val session = sessionManager.createSession(request, assertionResult.getCredential().getUserHandle());
                    return Either.right(
                        new SuccessfulAuthenticationResult(
                            assertionRequestWrapper,
                            assertionResponse,
                            userStorage.getRegistrationsByUsername(assertionResult.getUsername()),
                            assertionResult.getUsername(),
                            session
                        )
                    );
                } else {
                    return Either.left(AuthenticationFailure.of("Assertion failed: Invalid assertion."));
                }
            } catch (final AssertionFailedException e) {
                LOGGER.warn("Assertion failed", e);
                return Either.left(new AuthenticationFailure(List.of("Assertion failed", e.getMessage()),
                    isUnknownCredential(assertionRequestWrapper, assertionResponse)));
            } catch (final Exception e) {
                LOGGER.error("Assertion failed", e);
                return Either.left(AuthenticationFailure.of("Assertion failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    /**
     * Failed authentication.
     *
     * @param messages          the messages describing the failure
     * @param unknownCredential whether the asserted credential is no longer registered to its account
     */
    public record AuthenticationFailure(List<String> messages, boolean unknownCredential) {
        /**
         * Failure that does not concern an unknown credential.
         *
         * @param messages the messages
         * @return the authentication failure
         */
        public static AuthenticationFailure of(final String... messages) {
            return new AuthenticationFailure(List.of(messages), false);
        }
    }

    @Value
    public static class SuccessfulRegistrationResult {
        boolean success;

        RegistrationRequest request;

        RegistrationResponse response;

        CredentialRegistration registration;

        boolean attestationTrusted;

        Optional<AttestationCertInfo> attestationCert;

        @JsonSerialize(using = AuthDataSerializer.class)
        AuthenticatorData authData;

        String username;

        ByteArray sessionToken;

        public SuccessfulRegistrationResult(final RegistrationRequest request,
                                            final RegistrationResponse response,
                                            final CredentialRegistration registration,
                                            final boolean attestationTrusted,
                                            final ByteArray sessionToken) {
            this.request = request;
            this.response = response;
            this.registration = registration;
            this.attestationTrusted = attestationTrusted;
            attestationCert = Optional.ofNullable(
                    response.credential().getResponse().getAttestation().getAttestationStatement().get("x5c")
                ).map(certs -> certs.get(0))
                .flatMap(Unchecked.function(certDer -> Optional.of(new ByteArray(certDer.binaryValue()))))
                .map(AttestationCertInfo::new);
            this.authData = response.credential().getResponse().getParsedAuthenticatorData();
            this.username = request.username();
            this.sessionToken = sessionToken;
            this.success = true;
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

    /**
     * Whether the asserted credential is no longer registered to the account that owns it. The owner is the account
     * of the user handle in the assertion, or else the username of the request; the answer is read from that
     * account's registrations rather than from the repository's credential index, which may lag behind other nodes.
     * Nothing is reported when the owner cannot be found, since a credential the repository merely does not know
     * yet must never be signalled as unknown.
     *
     * @param assertionRequest  the assertion request
     * @param assertionResponse the assertion response
     * @return true if the owning account no longer holds the credential
     */
    private boolean isUnknownCredential(final AssertionRequestWrapper assertionRequest, final AssertionResponse assertionResponse) {
        try {
            val credential = assertionResponse.credential();
            return credential.getResponse().getUserHandle()
                .flatMap(userStorage::getUsernameForUserHandle)
                .or(assertionRequest::getUsername)
                .map(username -> userStorage.getRegistrationByUsernameAndCredentialId(username, credential.getId()).isEmpty())
                .orElse(Boolean.FALSE);
        } catch (final Exception e) {
            LOGGER.warn("Unable to determine whether the asserted credential is still registered", e);
            return false;
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
            result.isDiscoverable(),
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
        final Optional<Boolean> discoverable,
        final Optional<Attestation> attestationMetadata) {
        val reg = CredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname.orElse(null))
            .registrationTime(Clock.systemUTC().instant())
            .credential(credential)
            .transports(transports)
            .attestationMetadata(attestationMetadata.orElse(null))
            .discoverable(discoverable.orElse(null))
            .build();
        LOGGER.debug("Adding registration: user: [{}], nickname: [{}], credential: [{}]", userIdentity, nickname, credential);
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

}
