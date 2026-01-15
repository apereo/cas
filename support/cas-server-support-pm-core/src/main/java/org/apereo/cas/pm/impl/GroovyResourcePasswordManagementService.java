package org.apereo.cas.pm.impl;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyResourcePasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class GroovyResourcePasswordManagementService extends BasePasswordManagementService {

    private final ExecutableCompiledScript watchableScript;

    public GroovyResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                   final CasConfigurationProperties casProperties,
                                                   final Resource groovyResource,
                                                   final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public boolean changeInternal(final @NonNull PasswordChangeRequest bean) throws Throwable {
        return watchableScript.execute("change", Boolean.class, bean, LOGGER);
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) throws Throwable {
        return watchableScript.execute("findEmail", String.class, query, LOGGER);
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) throws Throwable {
        return watchableScript.execute("findPhone", String.class, query, LOGGER);
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) throws Throwable {
        return watchableScript.execute("findUsername", String.class, query, LOGGER);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        return watchableScript.execute("getSecurityQuestions", Map.class, query, LOGGER);
    }

    @Override
    public boolean unlockAccount(final Credential credential) throws Throwable {
        return watchableScript.execute("unlockAccount", Boolean.class, credential, LOGGER);
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        watchableScript.execute("updateSecurityQuestions", Void.class, query, LOGGER);
    }
}
