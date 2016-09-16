package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import java.util.Set;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private String endpoint;

    public RestMultifactorAuthenticationTrustStorage(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        return null;
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        return null;
    }
}
