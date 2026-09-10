package com.yubico.core;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yubico.data.CredentialRegistration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        val result = FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(Locale.ENGLISH), _ -> new HashSet<>()).add(reg));
        if (result) {
            invalidateCredentialIndex();
        }
        return result;
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        return FunctionUtils.doUnchecked(() -> storage.get(username.toLowerCase(Locale.ENGLISH), _ -> new HashSet<>()));
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        val result = FunctionUtils.doUnchecked(() ->
            storage.get(username.toLowerCase(Locale.ENGLISH), _ -> new HashSet<>()).remove(credentialRegistration));
        if (result) {
            invalidateCredentialIndex();
        }
        return result;
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        storage.invalidate(username.toLowerCase(Locale.ENGLISH));
        invalidateCredentialIndex();
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
