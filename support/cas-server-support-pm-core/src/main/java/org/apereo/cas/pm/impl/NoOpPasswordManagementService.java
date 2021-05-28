package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.InvalidPasswordException;
import org.apereo.cas.pm.PasswordChangeRequest;
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
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties) {
        super(passwordManagementProperties, cipherExecutor, issuer, null);
    }

    @Override
    public boolean changeInternal(final Credential credential, final PasswordChangeRequest bean) throws InvalidPasswordException {
        LOGGER.warn("Using no-op password change impl. Appropriate password management service is not configured.");
        return false;
    }
}
