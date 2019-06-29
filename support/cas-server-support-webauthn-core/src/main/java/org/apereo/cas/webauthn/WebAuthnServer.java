package org.apereo.cas.webauthn;

import org.apereo.cas.webauthn.attestation.DefaultAttestationCertificateTrustResolver;
import org.apereo.cas.webauthn.credential.CredentialRegistrationRequest;
import org.apereo.cas.webauthn.credential.InMemoryCachingWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.credential.WebAuthnCredentialRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.WebAuthnCodecs;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.attestation.MetadataObject;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.StandardMetadataService;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.CompositeAttestationResolver;
import com.yubico.webauthn.attestation.resolver.CompositeTrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolver;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.extension.appid.AppId;
import com.yubico.webauthn.extension.appid.InvalidAppIdException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link WebAuthnServer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class WebAuthnServer {
    private static final SecureRandom random = new SecureRandom();

    private static final String PREVIEW_METADATA_PATH = "/preview-metadata.json";

    private final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage;
    private final Cache<ByteArray, CredentialRegistrationRequest> registerRequestStorage;
    private final Cache<AssertionRequestWrapper, AuthenticatedAction> authenticatedActions = newCache();


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
    private final ObjectMapper jsonMapper = WebAuthnCodecs.json();

    private final WebAuthnCredentialRepository userStorage;
    private final RelyingParty relyingParty;
    
    public WebAuthnServer(final WebAuthnCredentialRepository userStorage,
                          final Cache<ByteArray, CredentialRegistrationRequest> registerRequestStorage,
                          final Cache<ByteArray, AssertionRequestWrapper> assertRequestStorage,
                          final RelyingPartyIdentity rpIdentity,
                          final Set<String> origins,
                          final Optional<AppId> appId) throws Exception {
        this.userStorage = userStorage;
        this.registerRequestStorage = registerRequestStorage;
        this.assertRequestStorage = assertRequestStorage;

        relyingParty = RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(this.userStorage)
            .origins(origins)
            .attestationConveyancePreference(Optional.of(AttestationConveyancePreference.DIRECT))
            .metadataService(Optional.of(metadataService))
            .allowUnrequestedExtensions(true)
            .allowUntrustedAttestation(true)
            .validateSignatureCounter(true)
            .appId(appId)
            .build();
    }

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    }
    
    private static ByteArray generateRandom(final int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

    private static MetadataObject readPreviewMetadata() {
        try (val is = WebAuthnServer.class.getResourceAsStream(PREVIEW_METADATA_PATH)) {
            return WebAuthnCodecs.json().readValue(is, MetadataObject.class);
        } catch (final IOException e) {
            throw ExceptionUtil.wrapAndLog(LOGGER, "Failed to read metadata from " + PREVIEW_METADATA_PATH, e);
        }
    }
    
    private static TrustResolver createExtraTrustResolver() {
        try {
            val metadata = readPreviewMetadata();
            val trustedCertificates = metadata.getParsedTrustedCertificates();
            val resolver = new SimpleTrustResolver(trustedCertificates);
            val trustedCerts = ArrayListMultimap.<String, X509Certificate>create();
            for (val cert : trustedCertificates) {
                trustedCerts.put(cert.getSubjectDN().getName(), cert);
            }
            return new DefaultAttestationCertificateTrustResolver(resolver, trustedCerts);
        } catch (final CertificateException e) {
            throw ExceptionUtil.wrapAndLog(LOGGER, "Failed to read trusted certificate(s)", e);
        }
    }

    public Either<String, CredentialRegistrationRequest> startRegistration(
        @NonNull String username,
        @NonNull String displayName,
        Optional<String> credentialNickname,
        boolean requireResidentKey
    ) {
        LOGGER.trace("startRegistration username: {}, credentialNickname: {}", username, credentialNickname);

        if (userStorage.getRegistrationsByUsername(username).isEmpty()) {
            val request = new CredentialRegistrationRequest(
                username,
                credentialNickname,
                generateRandom(32),
                relyingParty.startRegistration(
                    StartRegistrationOptions.builder()
                        .user(UserIdentity.builder()
                            .name(username)
                            .displayName(displayName)
                            .id(generateRandom(32))
                            .build()
                        )
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                            .requireResidentKey(requireResidentKey)
                            .build()
                        )
                        .build()
                )
            );
            registerRequestStorage.put(request.getRequestId(), request);
            return Either.right(request);
        } else {
            return Either.left("The username \"" + username + "\" is already registered.");
        }
    }
}
