package org.apereo.cas.pm.impl;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;

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
        super(cipherExecutor, issuer, passwordManagementProperties);
    }

    @Override
    public boolean changeInternal(final Credential c, final PasswordChangeBean bean) {
        return false;
    }

    @Override
    public String findEmail(final String username) {
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        return null;
    }
}
