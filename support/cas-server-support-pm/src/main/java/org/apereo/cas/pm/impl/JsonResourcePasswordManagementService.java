package org.apereo.cas.pm.impl;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link JsonResourcePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonResourcePasswordManagementService extends BasePasswordManagementService {

    private final Resource jsonResource;

    public JsonResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                 final String issuer,
                                                 final PasswordManagementProperties passwordManagementProperties,
                                                 final Resource jsonResource) {
        super(cipherExecutor, issuer, passwordManagementProperties);
        this.jsonResource = jsonResource;
    }

    @Override
    public boolean change(final Credential c, final PasswordChangeBean bean) {
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
