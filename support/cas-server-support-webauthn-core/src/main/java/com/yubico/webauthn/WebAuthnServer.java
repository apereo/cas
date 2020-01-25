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

package com.yubico.webauthn;

import com.yubico.webauthn.data.AssertionRequestWrapper;
import com.yubico.webauthn.data.AssertionResponse;
import com.yubico.webauthn.data.RegistrationRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Closeables;
import com.upokecenter.cbor.CBORObject;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.AttestationResolver;
import com.yubico.webauthn.attestation.MetadataObject;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.StandardMetadataService;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.CompositeAttestationResolver;
import com.yubico.webauthn.attestation.resolver.CompositeTrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleAttestationResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolverWithEquality;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorData;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.COSEAlgorithmIdentifier;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.yubico.webauthn.extension.appid.AppId;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.RegistrationResponse;
import com.yubico.webauthn.data.U2fRegistrationResponse;
import com.yubico.webauthn.data.U2fRegistrationResult;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebAuthnServer {
    private static final Logger logger = LoggerFactory.getLogger(WebAuthnServer.class);
    private static final SecureRandom random = new SecureRandom();

    private static final String PREVIEW_METADATA_PATH = "/preview-metadata.json";

    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage;
    private final Cache<ByteArray, RegistrationRequest> registerRequestStorage;
    private final RegistrationStorage userStorage;
    private final SessionManager sessions = new SessionManager();


    private final TrustResolver trustResolver = new CompositeTrustResolver(Arrays.asList(
        StandardMetadataService.createDefaultTrustResolver(),
        createExtraTrustResolver()
    ));

    private final MetadataService metadataService = new StandardMetadataService(
        new CompositeAttestationResolver(Arrays.asList(
            StandardMetadataService.createDefaultAttestationResolver(trustResolver),
            createExtraMetadataResolver(trustResolver)
        ))
    );

    private final Clock clock = Clock.systemDefaultZone();
    private final ObjectMapper jsonMapper = JacksonCodecs.json();

    private final RelyingParty rp;

    public WebAuthnServer() throws InvalidAppIdException, CertificateException {
        this(new InMemoryRegistrationStorage(), newCache(), newCache(), Config.getRpIdentity(), Config.getOrigins(), Config.getAppId());
    }

    public WebAuthnServer(RegistrationStorage userStorage, Cache<ByteArray, RegistrationRequest> registerRequestStorage, Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage, RelyingPartyIdentity rpIdentity, Set<String> origins, Optional<AppId> appId) throws InvalidAppIdException, CertificateException {
        this.userStorage = userStorage;
        this.registerRequestStorage = registerRequestStorage;
        this.assertRequestStorage = assertRequestStorage;

        rp = RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(this.userStorage)
            .origins(origins)
            .attestationConveyancePreference(Optional.of(AttestationConveyancePreference.DIRECT))
            .metadataService(Optional.of(metadataService))
            .allowOriginPort(false)
            .allowOriginSubdomain(false)
            .allowUnrequestedExtensions(true)
            .allowUntrustedAttestation(true)
            .validateSignatureCounter(true)
            .appId(appId)
            .build();
    }

    private static ByteArray generateRandom(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

    private static MetadataObject readPreviewMetadata() {
        InputStream is = WebAuthnServer.class.getResourceAsStream(PREVIEW_METADATA_PATH);
        try {
            return JacksonCodecs.json().readValue(is, MetadataObject.class);
        } catch (IOException e) {
            throw ExceptionUtil.wrapAndLog(logger, "Failed to read metadata from " + PREVIEW_METADATA_PATH, e);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    /**
     * Create a {@link TrustResolver} that accepts attestation certificates that are directly recognised as trust anchors.
     */
    private static TrustResolver createExtraTrustResolver() {
        try {
            MetadataObject metadata = readPreviewMetadata();
            return new SimpleTrustResolverWithEquality(metadata.getParsedTrustedCertificates());
        } catch (CertificateException e) {
            throw ExceptionUtil.wrapAndLog(logger, "Failed to read trusted certificate(s)", e);
        }
    }

    /**
     * Create a {@link AttestationResolver} with additional metadata for unreleased YubiKey Preview devices.
     */
    private static AttestationResolver createExtraMetadataResolver(TrustResolver trustResolver) {
        try {
            MetadataObject metadata = readPreviewMetadata();
            return new SimpleAttestationResolver(Collections.singleton(metadata), trustResolver);
        } catch (CertificateException e) {
            throw ExceptionUtil.wrapAndLog(logger, "Failed to read trusted certificate(s)", e);
        }
    }

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    }

    public Either<String, RegistrationRequest> startRegistration(
        @NonNull String username,
        Optional<String> displayName,
        Optional<String> credentialNickname,
        boolean requireResidentKey,
        Optional<ByteArray> sessionToken
    ) throws ExecutionException {
        logger.trace("startRegistration username: {}, credentialNickname: {}", username, credentialNickname);

        final Collection<CredentialRegistration> registrations = userStorage.getRegistrationsByUsername(username);
        final Optional<UserIdentity> existingUser =
            registrations.stream().findAny().map(CredentialRegistration::getUserIdentity);
        final boolean permissionGranted = existingUser
            .map(userIdentity ->
                sessions.isSessionForUser(userIdentity.getId(), sessionToken))
            .orElse(true);

        if (permissionGranted) {
            final UserIdentity registrationUserId = existingUser.orElseGet(() ->
                UserIdentity.builder()
                    .name(username)
                    .displayName(displayName.get())
                    .id(generateRandom(32))
                    .build()
            );

            RegistrationRequest request = new RegistrationRequest(
                username,
                credentialNickname,
                generateRandom(32),
                rp.startRegistration(
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
        } else {
            return Either.left("The username \"" + username + "\" is already registered.");
        }
    }

    @Value
    public static class SuccessfulRegistrationResult {
        final boolean success = true;
        RegistrationRequest request;
        RegistrationResponse response;
        CredentialRegistration registration;
        boolean attestationTrusted;
        Optional<AttestationCertInfo> attestationCert;
        @JsonSerialize(using = AuthDataSerializer.class)
        AuthenticatorData authData;
        String username;
        ByteArray sessionToken;

        public SuccessfulRegistrationResult(RegistrationRequest request, RegistrationResponse response, CredentialRegistration registration, boolean attestationTrusted, ByteArray sessionToken) {
            this.request = request;
            this.response = response;
            this.registration = registration;
            this.attestationTrusted = attestationTrusted;
            attestationCert = Optional.ofNullable(
                response.getCredential().getResponse().getAttestation().getAttestationStatement().get("x5c")
            ).map(certs -> certs.get(0))
            .flatMap((JsonNode certDer) -> {
                try {
                    return Optional.of(new ByteArray(certDer.binaryValue()));
                } catch (IOException e) {
                    logger.error("Failed to get binary value from x5c element: {}", certDer, e);
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
    public class SuccessfulU2fRegistrationResult {
        final boolean success = true;
        final RegistrationRequest request;
        final U2fRegistrationResponse response;
        final CredentialRegistration registration;
        boolean attestationTrusted;
        Optional<AttestationCertInfo> attestationCert;
        final String username;
        final ByteArray sessionToken;
    }

    @Value
    public static class AttestationCertInfo {
        final ByteArray der;
        final String text;
        public AttestationCertInfo(ByteArray certDer) {
            der = certDer;
            X509Certificate cert = null;
            try {
                cert = CertificateParser.parseDer(certDer.getBytes());
            } catch (CertificateException e) {
                logger.error("Failed to parse attestation certificate");
            }
            if (cert == null) {
                text = null;
            } else {
                text = cert.toString();
            }
        }
    }

    public Either<List<String>, SuccessfulRegistrationResult> finishRegistration(String responseJson) {
        logger.trace("finishRegistration responseJson: {}", responseJson);
        RegistrationResponse response = null;
        try {
            response = jsonMapper.readValue(responseJson, RegistrationResponse.class);
        } catch (IOException e) {
            logger.error("JSON error in finishRegistration; responseJson: {}", responseJson, e);
            return Either.left(Arrays.asList("Registration failed!", "Failed to decode response object.", e.getMessage()));
        }

        RegistrationRequest request = registerRequestStorage.getIfPresent(response.getRequestId());
        registerRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            logger.debug("fail finishRegistration responseJson: {}", responseJson);
            return Either.left(Arrays.asList("Registration failed!", "No such registration in progress."));
        } else {
            try {
                com.yubico.webauthn.RegistrationResult registration = rp.finishRegistration(
                    FinishRegistrationOptions.builder()
                        .request(request.getPublicKeyCredentialCreationOptions())
                        .response(response.getCredential())
                        .build()
                );

                if (userStorage.userExists(request.getUsername())) {
                    boolean permissionGranted = false;

                    final boolean isValidSession = request.getSessionToken().map(token ->
                        sessions.isSessionForUser(request.getPublicKeyCredentialCreationOptions().getUser().getId(), token)
                    ).orElse(false);

                    logger.debug("Session token: {}", request.getSessionToken());
                    logger.debug("Valid session: {}", isValidSession);

                    if (isValidSession) {
                        permissionGranted = true;
                        logger.info("Session token accepted for user {}", request.getPublicKeyCredentialCreationOptions().getUser().getId());
                    }

                    logger.debug("permissionGranted: {}", permissionGranted);

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
            } catch (RegistrationFailedException e) {
                logger.debug("fail finishRegistration responseJson: {}", responseJson, e);
                return Either.left(Arrays.asList("Registration failed!", e.getMessage()));
            } catch (Exception e) {
                logger.error("fail finishRegistration responseJson: {}", responseJson, e);
                return Either.left(Arrays.asList("Registration failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    public Either<List<String>, SuccessfulU2fRegistrationResult> finishU2fRegistration(String responseJson) throws ExecutionException {
        logger.trace("finishU2fRegistration responseJson: {}", responseJson);
        U2fRegistrationResponse response = null;
        try {
            response = jsonMapper.readValue(responseJson, U2fRegistrationResponse.class);
        } catch (IOException e) {
            logger.error("JSON error in finishU2fRegistration; responseJson: {}", responseJson, e);
            return Either.left(Arrays.asList("Registration failed!", "Failed to decode response object.", e.getMessage()));
        }

        RegistrationRequest request = registerRequestStorage.getIfPresent(response.getRequestId());
        registerRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            logger.debug("fail finishU2fRegistration responseJson: {}", responseJson);
            return Either.left(Arrays.asList("Registration failed!", "No such registration in progress."));
        } else {

            try {
                ExceptionUtil.assure(
                    U2fVerifier.verify(rp.getAppId().get(), request, response),
                    "Failed to verify signature."
                );
            } catch (Exception e) {
                logger.debug("Failed to verify U2F signature.", e);
                return Either.left(Arrays.asList("Failed to verify signature.", e.getMessage()));
            }

            X509Certificate attestationCert = null;
            try {
                attestationCert = CertificateParser.parseDer(response.getCredential().getU2fResponse().getAttestationCertAndSignature().getBytes());
            } catch (CertificateException e) {
                logger.error("Failed to parse attestation certificate: {}", response.getCredential().getU2fResponse().getAttestationCertAndSignature(), e);
            }

            Optional<Attestation> attestation = Optional.empty();
            try {
                if (attestationCert != null) {
                    attestation = Optional.of(metadataService.getAttestation(Collections.singletonList(attestationCert)));
                }
            } catch (CertificateEncodingException e) {
                logger.error("Failed to resolve attestation", e);
            }

            final U2fRegistrationResult result = U2fRegistrationResult.builder()
                .keyId(PublicKeyCredentialDescriptor.builder().id(response.getCredential().getU2fResponse().getKeyHandle()).build())
                .attestationTrusted(attestation.map(Attestation::isTrusted).orElse(false))
                .publicKeyCose(rawEcdaKeyToCose(response.getCredential().getU2fResponse().getPublicKey()))
                .attestationMetadata(attestation)
                .build();

            return Either.right(
                new SuccessfulU2fRegistrationResult(
                    request,
                    response,
                    addRegistration(
                        request.getPublicKeyCredentialCreationOptions().getUser(),
                        request.getCredentialNickname(),
                        0,
                        result
                    ),
                    result.isAttestationTrusted(),
                    Optional.of(new AttestationCertInfo(response.getCredential().getU2fResponse().getAttestationCertAndSignature())),
                    request.getUsername(),
                    sessions.createSession(request.getPublicKeyCredentialCreationOptions().getUser().getId())
                )
            );
        }
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(Optional<String> username) {
        logger.trace("startAuthentication username: {}", username);

        if (username.isPresent() && !userStorage.userExists(username.get())) {
            return Either.left(Collections.singletonList("The username \"" + username.get() + "\" is not registered."));
        } else {
            AssertionRequestWrapper request = new AssertionRequestWrapper(
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

    @Value
    @AllArgsConstructor
    public static final class SuccessfulAuthenticationResult {
        private final boolean success = true;
        private final AssertionRequestWrapper request;
        private final AssertionResponse response;
        private final Collection<CredentialRegistration> registrations;
        @JsonSerialize(using = AuthDataSerializer.class) AuthenticatorData authData;
        private final String username;
        private final ByteArray sessionToken;
        private final List<String> warnings;

        public SuccessfulAuthenticationResult(AssertionRequestWrapper request, AssertionResponse response, Collection<CredentialRegistration> registrations, String username, ByteArray sessionToken, List<String> warnings) {
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

    public Either<List<String>, SuccessfulAuthenticationResult> finishAuthentication(String responseJson) {
        logger.trace("finishAuthentication responseJson: {}", responseJson);

        final AssertionResponse response;
        try {
            response = jsonMapper.readValue(responseJson, AssertionResponse.class);
        } catch (IOException e) {
            logger.debug("Failed to decode response object", e);
            return Either.left(Arrays.asList("Assertion failed!", "Failed to decode response object.", e.getMessage()));
        }

        AssertionRequestWrapper request = assertRequestStorage.getIfPresent(response.getRequestId());
        assertRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            return Either.left(Arrays.asList("Assertion failed!", "No such assertion in progress."));
        } else {
            try {
                AssertionResult result = rp.finishAssertion(
                    FinishAssertionOptions.builder()
                        .request(request.getRequest())
                        .response(response.getCredential())
                        .build()
                );

                if (result.isSuccess()) {
                    try {
                        userStorage.updateSignatureCount(result);
                    } catch (Exception e) {
                        logger.error(
                            "Failed to update signature count for user \"{}\", credential \"{}\"",
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
                } else {
                    return Either.left(Collections.singletonList("Assertion failed: Invalid assertion."));
                }
            } catch (AssertionFailedException e) {
                logger.debug("Assertion failed", e);
                return Either.left(Arrays.asList("Assertion failed!", e.getMessage()));
            } catch (Exception e) {
                logger.error("Assertion failed", e);
                return Either.left(Arrays.asList("Assertion failed unexpectedly; this is likely a bug.", e.getMessage()));
            }
        }
    }

    @Value
    public static final class DeregisterCredentialResult {
        boolean success = true;
        CredentialRegistration droppedRegistration;
        boolean accountDeleted;
    }

    public Either<List<String>, DeregisterCredentialResult> deregisterCredential(
        @NonNull ByteArray sessionToken,
        ByteArray credentialId
    ) {
        logger.trace("deregisterCredential session: {}, credentialId: {}", sessionToken, credentialId);

        if (credentialId == null || credentialId.getBytes().length == 0) {
            return Either.left(Collections.singletonList("Credential ID must not be empty."));
        }

        Optional<ByteArray> session = sessions.getSession(sessionToken);
        if (session.isPresent()) {
            ByteArray userHandle = session.get();
            Optional<String> username = userStorage.getUsernameForUserHandle(userHandle);

            if (username.isPresent()) {
                Optional<CredentialRegistration> credReg = userStorage.getRegistrationByUsernameAndCredentialId(username.get(), credentialId);
                if (credReg.isPresent()) {
                    userStorage.removeRegistrationByUsername(username.get(), credReg.get());

                    return Either.right(new DeregisterCredentialResult(
                        credReg.get(),
                        !userStorage.userExists(username.get())
                    ));
                } else {
                    return Either.left(Collections.singletonList("Credential ID not registered:" + credentialId));
                }
            } else {
                return Either.left(Collections.singletonList("Invalid user handle"));
            }
        } else {
            return Either.left(Collections.singletonList("Invalid session"));
        }
    }

    public <T> Either<List<String>, T> deleteAccount(String username, Supplier<T> onSuccess) {
        logger.trace("deleteAccount username: {}", username);

        if (username == null || username.isEmpty()) {
            return Either.left(Collections.singletonList("Username must not be empty."));
        }

        boolean removed = userStorage.removeAllRegistrations(username);

        if (removed) {
            return Either.right(onSuccess.get());
        } else {
            return Either.left(Collections.singletonList("Username not registered:" + username));
        }
    }

    private CredentialRegistration addRegistration(
        UserIdentity userIdentity,
        Optional<String> nickname,
        RegistrationResponse response,
        RegistrationResult result
    ) {
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
        UserIdentity userIdentity,
        Optional<String> nickname,
        long signatureCount,
        U2fRegistrationResult result
    ) {
        return addRegistration(
            userIdentity,
            nickname,
            signatureCount,
            RegisteredCredential.builder()
                .credentialId(result.getKeyId().getId())
                .userHandle(userIdentity.getId())
                .publicKeyCose(result.getPublicKeyCose())
                .signatureCount(signatureCount)
                .build(),
            result.getAttestationMetadata()
        );
    }

    private CredentialRegistration addRegistration(
        UserIdentity userIdentity,
        Optional<String> nickname,
        long signatureCount,
        RegisteredCredential credential,
        Optional<Attestation> attestationMetadata
    ) {
        CredentialRegistration reg = CredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname)
            .registrationTime(clock.instant())
            .credential(credential)
            .signatureCount(signatureCount)
            .attestationMetadata(attestationMetadata)
            .build();

        logger.debug(
            "Adding registration: user: {}, nickname: {}, credential: {}",
            userIdentity,
            nickname,
            credential
        );
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }

    static ByteArray rawEcdaKeyToCose(ByteArray key) {
        final byte[] keyBytes = key.getBytes();

        if (!(keyBytes.length == 64 || (keyBytes.length == 65 && keyBytes[0] == 0x04))) {
            throw new IllegalArgumentException(String.format(
                "Raw key must be 64 bytes long or be 65 bytes long and start with 0x04, was %d bytes starting with %02x",
                keyBytes.length,
                keyBytes[0]
            ));
        }

        final int start = keyBytes.length == 64 ? 0 : 1;

        Map<Long, Object> coseKey = new HashMap<>();

        coseKey.put(1L, 2L); // Key type: EC
        coseKey.put(3L, COSEAlgorithmIdentifier.ES256.getId());
        coseKey.put(-1L, 1L); // Curve: P-256
        coseKey.put(-2L, Arrays.copyOfRange(keyBytes, start, start + 32)); // x
        coseKey.put(-3L, Arrays.copyOfRange(keyBytes, start + 32, start + 64)); // y

        return new ByteArray(CBORObject.FromObject(coseKey).EncodeToBytes());
    }


    private static class AuthDataSerializer extends JsonSerializer<AuthenticatorData> {
        @Override public void serialize(AuthenticatorData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            gen.writeObjectField("extensions", value.getExtensions());
            gen.writeEndObject();
        }
    }

}
