package org.apereo.cas.pm.impl;

import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link NoOpPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpPasswordManagementService extends BasePasswordManagementService {
    public NoOpPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final PasswordManagementProperties passwordManagementProperties) {
        super(passwordManagementProperties, cipherExecutor, issuer, null);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        return null;
    }
}
