package org.apereo.cas.trusted.authentication.impl;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.trusted.authentication.AuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InMemoryMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {
    private CipherExecutor<String, String> cipherExecutor;

    private Map<String, AuthenticationTrustRecord> storage = Maps.newConcurrentMap();

    @Override
    public Set<AuthenticationTrustRecord> get(final String principal) {
        return storage.values()
                .stream()
                .filter(entry -> entry.getPrincipal().equalsIgnoreCase(principal))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AuthenticationTrustRecord> get(final String principal, final LocalDate onOrAfterDate) {
        final Set<AuthenticationTrustRecord> res = get(principal);
        res.removeIf(entry -> entry.getDate().isBefore(onOrAfterDate)
                && StringUtils.isNotBlank(this.cipherExecutor.decode(entry.getKey())));
        return res;
    }

    @Override
    public void set(final AuthenticationTrustRecord record) {
        final String key = cipherExecutor.encode(UUID.randomUUID().toString());
        storage.put(key, record);
    }

    public void setCipherExecutor(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }
}
