package org.apereo.cas.authentication.support.password;

import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * This is {@link GroovyPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyPasswordPolicyHandlingStrategy<AuthenticationResponse> implements
    AuthenticationPasswordPolicyHandlingStrategy<AuthenticationResponse, PasswordPolicyConfiguration> {
    private final transient Resource groovyResource;

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final PasswordPolicyConfiguration configuration) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        return ScriptingUtils.executeGroovyScript(groovyResource,
            new Object[]{response, configuration, LOGGER, applicationContext}, List.class, true);
    }
}
