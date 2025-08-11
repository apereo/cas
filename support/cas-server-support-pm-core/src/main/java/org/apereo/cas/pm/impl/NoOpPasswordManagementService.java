package org.apereo.cas.pm.impl;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.impl.history.InMemoryPasswordHistoryService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;

/**
 * This is {@link NoOpPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class NoOpPasswordManagementService extends BasePasswordManagementService {
    public NoOpPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final CasConfigurationProperties casProperties) {
        super(casProperties, cipherExecutor, new InMemoryPasswordHistoryService());
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) {
        LOGGER.warn("Using no-op password change implementation. Appropriate password management service is not configured.");
        return false;
    }
}
