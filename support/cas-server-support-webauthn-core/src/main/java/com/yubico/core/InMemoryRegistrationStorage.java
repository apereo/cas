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

import org.apereo.cas.util.function.FunctionUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.CollectionUtil;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryRegistrationStorage implements RegistrationStorage, CredentialRepository {

    @Getter
    private final Cache<String, Set<CredentialRegistration>> storage = CacheBuilder.newBuilder()
        .maximumSize(5000)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    @Override
    public boolean addRegistrationByUsername(final String username, final CredentialRegistration reg) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(), HashSet::new).add(reg));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        return getRegistrationsByUsername(username.toLowerCase()).stream()
            .map(registration -> PublicKeyCredentialDescriptor.builder()
                .id(registration.getCredential().getCredentialId())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(), HashSet::new));
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
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream()
            .findAny()
            .map(CredentialRegistration::getUsername);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        return getRegistrationsByUsername(username.toLowerCase()).stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public void updateSignatureCount(final AssertionResult result) {
        var registration = getRegistrationByUsernameAndCredentialId(result.getUsername().toLowerCase(Locale.ROOT), result.getCredentialId())
            .orElseThrow(() -> new NoSuchElementException(String.format(
                "Credential \"%s\" is not registered to user \"%s\"",
                result.getCredentialId(), result.getUsername()
            )));

        var regs = storage.getIfPresent(result.getUsername().toLowerCase());
        Objects.requireNonNull(regs).remove(registration);
        regs.add(registration.withSignatureCount(result.getSignatureCount()));
    }

    @Override
    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(final String username, final ByteArray id) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(), HashSet::new).stream()
            .filter(credReg -> id.equals(credReg.getCredential().getCredentialId()))
            .findFirst());
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(), HashSet::new).remove(credentialRegistration));
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        storage.invalidate(username.toLowerCase());
        return true;
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle) {
        var registrationMaybe = storage.asMap().values().stream()
            .flatMap(Collection::stream)
            .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
            .findAny();

        LOGGER.debug("Lookup credential ID: [{}], user handle: [{}]; result: [{}]",
            credentialId, userHandle, registrationMaybe);
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
            storage.asMap().values().stream()
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
