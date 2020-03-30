package com.yubico.webauthn;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryRegistrationStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class InMemoryRegistrationStorage implements RegistrationStorage, CredentialRepository {

    private final Cache<String, Set<CredentialRegistration>> storage = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build();

    @Override
    public boolean addRegistrationByUsername(final String username, final CredentialRegistration reg) {
        try {
            return storage.get(username, HashSet::new).add(reg);
        } catch (final ExecutionException e) {
            LOGGER.error("Failed to add registration", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        try {
            return storage.get(username, HashSet::new);
        } catch (final ExecutionException e) {
            LOGGER.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(final String username, final ByteArray id) {
        try {
            return storage.get(username, HashSet::new).stream()
                .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()))
                .findFirst();
        } catch (final ExecutionException e) {
            LOGGER.error("Registration lookup failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUserHandle(final ByteArray userHandle) {
        return storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(credentialRegistration ->
                userHandle.equals(credentialRegistration.getUserIdentity().getId())
            )
            .collect(Collectors.toList());
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        try {
            return storage.get(username, HashSet::new).remove(credentialRegistration);
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
    public void updateSignatureCount(final AssertionResult result) {
        val registration = getRegistrationByUsernameAndCredentialId(result.getUsername(), result.getCredentialId())
            .orElseThrow(() -> new NoSuchElementException(String.format(
                "Credential \"%s\" is not registered to user \"%s\"",
                result.getCredentialId(), result.getUsername()
            )));

        val regs = storage.getIfPresent(result.getUsername());
        Objects.requireNonNull(regs).remove(registration);
        regs.add(registration.withSignatureCount(result.getSignatureCount()));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        return getRegistrationsByUsername(username).stream()
            .map(registration -> PublicKeyCredentialDescriptor.builder()
                .id(registration.getCredential().getCredentialId())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        return getRegistrationsByUsername(username).stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream()
            .findAny()
            .map(CredentialRegistration::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle) {
        final Optional<CredentialRegistration> registrationMaybe = storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
            .findAny();
        LOGGER.trace("lookup credential ID: {}, user handle: {}; result: {}", credentialId, userHandle, registrationMaybe);
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
        return storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
            .map(reg -> RegisteredCredential.builder()
                .credentialId(reg.getCredential().getCredentialId())
                .userHandle(reg.getUserIdentity().getId())
                .publicKeyCose(reg.getCredential().getPublicKeyCose())
                .signatureCount(reg.getSignatureCount())
                .build()
            )
            .collect(Collectors.toSet());
    }

}
