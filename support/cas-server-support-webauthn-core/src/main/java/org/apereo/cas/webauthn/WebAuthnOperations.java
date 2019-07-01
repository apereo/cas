package org.apereo.cas.webauthn;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.webauthn.attestation.AttestationCertInfo;
import org.apereo.cas.webauthn.authentication.AssertionRequestWrapper;
import org.apereo.cas.webauthn.authentication.AssertionResponse;
import org.apereo.cas.webauthn.authentication.AuthenticatedAction;
import org.apereo.cas.webauthn.authentication.SuccessfulAuthenticationResult;
import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;
import org.apereo.cas.webauthn.credential.repository.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.registration.WebAuthnCredentialRegistrationResponse;
import org.apereo.cas.webauthn.registration.WebAuthnRegistrationRequest;
import org.apereo.cas.webauthn.registration.WebAuthnRegistrationResponse;
import org.apereo.cas.webauthn.registration.WebAuthnRegistrationResult;
import org.apereo.cas.webauthn.registration.WebAuthnSuccessfulRegistrationResult;
import org.apereo.cas.webauthn.registration.WebAuthnSuccessfulU2fRegistrationResult;
import org.apereo.cas.webauthn.util.Either;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.WebAuthnCodecs;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.WebAuthnVerifier;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is {@link WebAuthnOperations}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class WebAuthnOperations {

    private static final int RANDOM_KEY_LENGTH = 32;


    private static final ObjectMapper MAPPER = WebAuthnCodecs.json().findAndRegisterModules();

    private final WebAuthnCredentialRepository userStorage;
    private final Cache<ByteArray, WebAuthnRegistrationRequest> registerRequestStorage;
    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage;
    private final Cache<AssertionRequestWrapper, AuthenticatedAction> authenticatedActions;
    private final RelyingParty relyingParty;
    private final MetadataService metadataService;

    private static ByteArray generateRandom() {
        val bytes = new byte[RANDOM_KEY_LENGTH];
        RandomUtils.getNativeInstance().nextBytes(bytes);
        return new ByteArray(bytes);
    }

    public Either<String, WebAuthnRegistrationRequest> startRegistration(final String username, final String displayName,
                                                                         final Optional<String> credentialNickname,
                                                                         final boolean requireResidentKey) {
        if (userStorage.getRegistrationsByUsername(username).isEmpty()) {
            var registration = StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                    .name(username)
                    .displayName(displayName)
                    .id(generateRandom())
                    .build()
                )
                .authenticatorSelection(authSelectionCriteria(requireResidentKey)
                )
                .build();
            val request = new WebAuthnRegistrationRequest(
                username,
                credentialNickname,
                generateRandom(),
                relyingParty.startRegistration(registration)
            );
            registerRequestStorage.put(request.getRequestId(), request);
            return Either.right(request);
        }
        return Either.left("The username \"" + username + "\" is already registered.");
    }

    private AuthenticatorSelectionCriteria authSelectionCriteria(final boolean requireResidentKey) {
        return AuthenticatorSelectionCriteria.builder()
            .requireResidentKey(requireResidentKey)
            .build();
    }

    public <T> Either<List<String>, AssertionRequestWrapper> startAddCredential(
        final String username,
        final Optional<String> credentialNickname,
        final boolean requireResidentKey,
        final Function<WebAuthnRegistrationRequest, Either<List<String>, T>> whenAuthenticated) {

        if (username == null || username.isEmpty()) {
            return Either.left(CollectionUtils.wrapList("username must not be empty."));
        }

        val registrations = userStorage.getRegistrationsByUsername(username);
        if (registrations.isEmpty()) {
            return Either.left(CollectionUtils.wrapList("The username " + username + " is not registered."));
        }
        val existingUser = registrations.stream().findAny().get().getUserIdentity();
        AuthenticatedAction<T> action = (SuccessfulAuthenticationResult result) -> {
            var selectionCriteria = AuthenticatorSelectionCriteria.builder()
                .requireResidentKey(requireResidentKey)
                .build();
            val registration = StartRegistrationOptions.builder()
                .user(existingUser)
                .authenticatorSelection(selectionCriteria)
                .build();
            val request = new WebAuthnRegistrationRequest(
                username,
                credentialNickname,
                generateRandom(),
                relyingParty.startRegistration(registration)
            );
            registerRequestStorage.put(request.getRequestId(), request);
            return whenAuthenticated.apply(request);
        };

        return startAuthenticatedAction(Optional.of(username), action);
    }

    public Either<List<String>, WebAuthnSuccessfulRegistrationResult> finishRegistration(final String responseJson) {
        try {
            val response = MAPPER.readValue(responseJson, WebAuthnRegistrationResponse.class);
            val request = registerRequestStorage.getIfPresent(response.getRequestId());
            registerRequestStorage.invalidate(response.getRequestId());

            if (request == null) {
                return Either.left(CollectionUtils.wrapList("No such registration in progress."));
            }
            var finishedRegistration = FinishRegistrationOptions.builder()
                .request(request.getPublicKeyCredentialCreationOptions())
                .response(response.getCredential())
                .build();
            val registration = relyingParty.finishRegistration(finishedRegistration);

            val addedRegistration = addRegistration(
                request.getPublicKeyCredentialCreationOptions().getUser(),
                request.getCredentialNickname(),
                response,
                registration
            );
            val success = new WebAuthnSuccessfulRegistrationResult(
                request,
                response,
                addedRegistration,
                registration.isAttestationTrusted()
            );
            return Either.right(success);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Either.left(CollectionUtils.wrapList(e.getMessage()));
        }
    }

    public Either<List<String>, WebAuthnSuccessfulU2fRegistrationResult> finishU2fRegistration(final String responseJson) {
        var response = (WebAuthnCredentialRegistrationResponse) null;
        try {
            response = MAPPER.readValue(responseJson, WebAuthnCredentialRegistrationResponse.class);
        } catch (final IOException e) {
            LOGGER.error("JSON error in finishU2fRegistration; responseJson: {}", responseJson, e);
            return Either.left(CollectionUtils.wrapList("Failed to decode response object.", e.getMessage()));
        }

        val request = registerRequestStorage.getIfPresent(response.getRequestId());
        registerRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            LOGGER.debug("fail finishU2fRegistration responseJson: {}", responseJson);
            return Either.left(CollectionUtils.wrapList("Registration failed!", "No such registration in progress."));
        }
        try {
            ExceptionUtil.assure(WebAuthnVerifier.verify(relyingParty.getAppId().get(), request, response), "Failed to verify signature.");
        } catch (final Exception e) {
            LOGGER.debug("Failed to verify U2F signature.", e);
            return Either.left(CollectionUtils.wrapList("Failed to verify signature.", e.getMessage()));
        }

        var attestationCert = (X509Certificate) null;
        try {
            attestationCert = CertificateParser.parseDer(response.getCredential().getU2fResponse().getAttestationCertAndSignature().getBytes());
        } catch (final CertificateException e) {
            LOGGER.error("Failed to parse attestation certificate: {}", response.getCredential().getU2fResponse().getAttestationCertAndSignature(), e);
        }

        Optional<Attestation> attestation = Optional.empty();
        try {
            if (attestationCert != null) {
                attestation = Optional.of(metadataService.getAttestation(CollectionUtils.wrapList(attestationCert)));
            }
        } catch (final CertificateEncodingException e) {
            LOGGER.error("Failed to resolve attestation", e);
        }

        val result = WebAuthnRegistrationResult.builder()
            .keyId(PublicKeyCredentialDescriptor.builder().id(response.getCredential().getU2fResponse().getKeyHandle()).build())
            .attestationTrusted(attestation.map(Attestation::isTrusted).orElse(false))
            .publicKeyCose(WebAuthnCodecs.rawEcdaKeyToCose(response.getCredential().getU2fResponse().getPublicKey()))
            .attestationMetadata(attestation)
            .build();

        val credentialRegistration = addRegistration(
            request.getPublicKeyCredentialCreationOptions().getUser(),
            request.getCredentialNickname(),
            0,
            result
        );

        val success = new WebAuthnSuccessfulU2fRegistrationResult(
            request,
            response,
            credentialRegistration,
            result.isAttestationTrusted(),
            Optional.of(new AttestationCertInfo(response.getCredential().getU2fResponse().getAttestationCertAndSignature()))
        );
        return Either.right(success);
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthentication(final Optional<String> username) {
        if (username.isPresent() && userStorage.getRegistrationsByUsername(username.get()).isEmpty()) {
            return Either.left(CollectionUtils.wrapList("The username " + username.get() + " is not registered."));
        }
        val request = new AssertionRequestWrapper(
            generateRandom(),
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
            response = MAPPER.readValue(responseJson, AssertionResponse.class);
        } catch (final IOException e) {
            LOGGER.debug("Failed to decode response object", e);
            return Either.left(CollectionUtils.wrapList("Failed to decode response object.", e.getMessage()));
        }

        val request = assertRequestStorage.getIfPresent(response.getRequestId());
        assertRequestStorage.invalidate(response.getRequestId());

        if (request == null) {
            return Either.left(CollectionUtils.wrapList("No such assertion in progress."));
        }
        try {
            val finishedAssertion = FinishAssertionOptions.builder()
                .request(request.getRequest())
                .response(response.getCredential())
                .build();
            val result = relyingParty.finishAssertion(finishedAssertion);

            if (result.isSuccess()) {
                try {
                    userStorage.updateSignatureCount(result);
                } catch (final Exception e) {
                    LOGGER.error(
                        "Failed to update signature count for user [{}], credential [{}]",
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
                        result.getWarnings()
                    )
                );
            }
            return Either.left(CollectionUtils.wrapList("Assertion failed: Invalid assertion."));
        } catch (final AssertionFailedException e) {
            LOGGER.debug("Assertion failed", e);
            return Either.left(CollectionUtils.wrapList(e.getMessage()));
        } catch (final Exception e) {
            LOGGER.error("Assertion failed", e);
            return Either.left(CollectionUtils.wrapList(e.getMessage()));
        }
    }

    public Either<List<String>, AssertionRequestWrapper> startAuthenticatedAction(final Optional<String> username,
                                                                                  final AuthenticatedAction<?> action) {
        return startAuthentication(username)
            .map(request -> {
                synchronized (authenticatedActions) {
                    authenticatedActions.put(request, action);
                }
                return request;
            });
    }

    public Either<List<String>, ?> finishAuthenticatedAction(final String responseJson) {
        return finishAuthentication(responseJson)
            .flatMap(result -> {
                val action = (AuthenticatedAction<?>) authenticatedActions.getIfPresent(result.getRequest());
                authenticatedActions.invalidate(result.getRequest());
                if (action == null) {
                    return Either.left(CollectionUtils.wrapList("No action was associated with assertion request ID: " + result.getRequest().getRequestId()));
                }
                return action.apply(result);
            });
    }

    public <T> Either<List<String>, AssertionRequestWrapper> deregisterCredential(final String username,
                                                                                  final ByteArray credentialId,
                                                                                  final Function<WebAuthnCredentialRegistration, T> resultMapper) {
        if (username == null || username.isEmpty()) {
            return Either.left(CollectionUtils.wrapList("Username must not be empty."));
        }

        if (credentialId == null || credentialId.getBytes().length == 0) {
            return Either.left(CollectionUtils.wrapList("Credential ID must not be empty."));
        }

        AuthenticatedAction<T> action = (SuccessfulAuthenticationResult result) -> {
            val credReg = userStorage.getRegistrationByUsernameAndCredentialId(username, credentialId);
            if (credReg.isPresent()) {
                userStorage.removeRegistrationByUsername(username, credReg.get());
                return Either.right(resultMapper.apply(credReg.get()));
            }
            return Either.left(CollectionUtils.wrapList("Credential ID not registered:" + credentialId));
        };

        return startAuthenticatedAction(Optional.of(username), action);
    }

    public <T> Either<List<String>, T> deleteAccount(final String username, final Supplier<T> onSuccess) {
        if (username == null || username.isEmpty()) {
            return Either.left(CollectionUtils.wrapList("Username must not be empty."));
        }
        val removed = userStorage.removeAllRegistrations(username);
        if (removed) {
            return Either.right(onSuccess.get());
        }
        return Either.left(CollectionUtils.wrapList("Username not registered:" + username));
    }

    private WebAuthnCredentialRegistration addRegistration(final UserIdentity userIdentity, final Optional<String> nickname,
                                                           final WebAuthnRegistrationResponse response, final RegistrationResult result) {
        val credential = RegisteredCredential.builder()
            .credentialId(result.getKeyId().getId())
            .userHandle(userIdentity.getId())
            .publicKeyCose(result.getPublicKeyCose())
            .signatureCount(response.getCredential().getResponse().getParsedAuthenticatorData().getSignatureCounter())
            .build();

        return addRegistration(
            userIdentity,
            nickname,
            response.getCredential().getResponse().getAttestation().getAuthenticatorData().getSignatureCounter(),
            credential,
            result.getAttestationMetadata()
        );
    }

    private WebAuthnCredentialRegistration addRegistration(
        final UserIdentity userIdentity,
        final Optional<String> nickname,
        final long signatureCount,
        final WebAuthnRegistrationResult result) {
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

    private WebAuthnCredentialRegistration addRegistration(final UserIdentity userIdentity,
                                                           final Optional<String> nickname,
                                                           final long signatureCount,
                                                           final RegisteredCredential credential,
                                                           final Optional<Attestation> attestationMetadata) {

        val reg = WebAuthnCredentialRegistration.builder()
            .userIdentity(userIdentity)
            .credentialNickname(nickname)
            .registrationTime(ZonedDateTime.now(ZoneOffset.UTC).toInstant())
            .credential(credential)
            .signatureCount(signatureCount)
            .attestationMetadata(attestationMetadata)
            .build();
        userStorage.addRegistrationByUsername(userIdentity.getName(), reg);
        return reg;
    }
}
