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
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.data.CredentialRegistration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Getter
public class InMemoryRegistrationStorage extends BaseWebAuthnCredentialRepository {

    private final Cache<String, Set<CredentialRegistration>> storage = Caffeine.newBuilder()
        .maximumSize(5000)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build();

    public InMemoryRegistrationStorage(final CasConfigurationProperties properties,
                                          final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
    }

    @Override
    public boolean addRegistrationByUsername(final String username, final CredentialRegistration reg) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(Locale.ENGLISH), __ -> new HashSet<>()).add(reg));
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(Locale.ENGLISH), __ -> new HashSet<>()));
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(Locale.ENGLISH), __ -> new HashSet<>()).remove(credentialRegistration));
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        storage.invalidate(username.toLowerCase(Locale.ENGLISH));
        return true;
    }

    @Override
    public Stream<? extends CredentialRegistration> stream() {
        return storage.asMap().values().stream().flatMap(Set::stream);
    }

    @Override
    protected void update(final String username, final Collection<CredentialRegistration> records) {
        storage.put(username.toLowerCase(Locale.ENGLISH), new LinkedHashSet<>(records));
    }
}
