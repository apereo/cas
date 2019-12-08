package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link GroovyResourcePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class GroovyResourcePasswordManagementService extends BasePasswordManagementService {

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                   final String issuer,
                                                   final PasswordManagementProperties passwordManagementProperties,
                                                   final Resource groovyResource,
                                                   final PasswordHistoryService passwordHistoryService) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean changeInternal(final @NonNull Credential credential, final @NonNull PasswordChangeRequest bean) {
        return watchableScript.execute("change", Boolean.class, new Object[]{credential, bean, LOGGER});
    }

    @Override
    public String findEmail(final String username) {
        return watchableScript.execute("findEmail", String.class, new Object[]{username, LOGGER});
    }

    @Override
    public String findPhone(final String username) {
        return watchableScript.execute("findPhone", String.class, new Object[]{username, LOGGER});
    }

    @Override
    public String findUsername(final String email) {
        return watchableScript.execute("findUsername", String.class, new Object[]{email, LOGGER});
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        return watchableScript.execute("getSecurityQuestions", Map.class, new Object[]{username, LOGGER});
    }
}
