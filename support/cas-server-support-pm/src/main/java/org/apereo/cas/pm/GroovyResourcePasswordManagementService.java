package org.apereo.cas.pm;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.util.ScriptingUtils;

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

    private final Resource groovyResource;

    public GroovyResourcePasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                                   final String issuer,
                                                   final PasswordManagementProperties passwordManagementProperties,
                                                   final Resource jsonResource) {
        super(passwordManagementProperties, cipherExecutor, issuer);
        this.groovyResource = jsonResource;
    }

    @Override
    public boolean changeInternal(@NonNull final Credential credential, @NonNull final PasswordChangeBean bean) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, "change", new Object[]{credential, bean, LOGGER}, Boolean.class, true);
    }

    @Override
    public String findEmail(final String username) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, "findEmail", new Object[]{username, LOGGER}, String.class, true);
    }

    @Override
    public String findUsername(final String email) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, "findUsername", new Object[]{email, LOGGER}, String.class, true);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, "getSecurityQuestions", new Object[]{username, LOGGER}, Map.class, true);
    }
}
