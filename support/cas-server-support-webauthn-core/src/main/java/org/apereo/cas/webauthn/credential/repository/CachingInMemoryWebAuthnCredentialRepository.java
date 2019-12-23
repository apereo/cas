package org.apereo.cas.webauthn.credential.repository;

import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.internal.util.CollectionUtil;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link CachingInMemoryWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class CachingInMemoryWebAuthnCredentialRepository implements WebAuthnCredentialRepository {
    private static final int CACHE_MAX_SIZE = 10_000;
    private static final int CACHE_EXPIRE_DAYS = 30;

    private final Cache<String, Set<WebAuthnCredentialRegistration>> storage = CacheBuilder.newBuilder()
        .maximumSize(CACHE_MAX_SIZE)
        .expireAfterAccess(Duration.ofDays(CACHE_EXPIRE_DAYS))
        .build();

    @Override
    public boolean addRegistrationByUsername(final String username, final WebAuthnCredentialRegistration reg) {
        try {
            return storage.get(username, HashSet::new).add(reg);
        } catch (final ExecutionException e) {
            LOGGER.error("Failed to add registration", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        return getRegistrationsByUsername(username)
            .stream()
            .map(registration -> PublicKeyCredentialDescriptor.builder()
                .id(registration.getCredential().getCredentialId())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<WebAuthnCredentialRegistration> getRegistrationsByUsername(final String username) {
        try {
            return storage.get(username, HashSet::new);
        } catch (final ExecutionException e) {
            LOGGER.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<WebAuthnCredentialRegistration> getRegistrationsByUserHandle(final ByteArray userHandle) {
        return storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(request -> userHandle.equals(request.getUserIdentity().getId())
            )
            .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream()
            .findAny()
            .map(WebAuthnCredentialRegistration::getUsername);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        return getRegistrationsByUsername(username).stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public void updateSignatureCount(final AssertionResult result) {
        val registration = getRegistrationByUsernameAndCredentialId(result.getUsername(), result.getCredentialId())
            .orElseThrow(() -> new NoSuchElementException(String.format(
                "Credential \"%s\" is not registered to user \"%s\"",
                result.getCredentialId(), result.getUsername()
            )));

        val regs = storage.getIfPresent(result.getUsername());
        if (regs != null) {
            regs.remove(registration);
            registration.setSignatureCount(result.getSignatureCount());
            regs.add(registration);
        }
    }

    @Override
    public Optional<WebAuthnCredentialRegistration> getRegistrationByUsernameAndCredentialId(final String username,
                                                                                             final ByteArray id) {
        try {
            return storage.get(username, HashSet::new)
                .stream()
                .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()))
                .findFirst();
        } catch (final ExecutionException e) {
            LOGGER.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeRegistrationByUsername(final String username,
                                                final WebAuthnCredentialRegistration request) {
        try {
            return storage.get(username, HashSet::new).remove(request);
        } catch (final ExecutionException e) {
            LOGGER.error("Failed to remove registration", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        storage.invalidate(username);
        return true;
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId,
                                                 final ByteArray userHandle) {
        val registrationMaybe = storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
            .findAny();
        return registrationMaybe.flatMap(registration ->
            Optional.of(
                RegisteredCredential.builder()
                    .credentialId(registration.getCredential().getCredentialId())
                    .userHandle(registration.getUserIdentity().getId())
                    .publicKeyCose(registration.getCredential().getPublicKeyCose())
                    .signatureCount(registration.getSignatureCount())
                    .build()
            )
        );
    }

    @Override
    public Set<RegisteredCredential> lookupAll(final ByteArray credentialId) {
        return CollectionUtil.immutableSet(
            storage.asMap()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
                .map(reg -> RegisteredCredential.builder()
                    .credentialId(reg.getCredential().getCredentialId())
                    .userHandle(reg.getUserIdentity().getId())
                    .publicKeyCose(reg.getCredential().getPublicKeyCose())
                    .signatureCount(reg.getSignatureCount())
                    .build()
                )
                .collect(Collectors.toSet()));
    }
}
