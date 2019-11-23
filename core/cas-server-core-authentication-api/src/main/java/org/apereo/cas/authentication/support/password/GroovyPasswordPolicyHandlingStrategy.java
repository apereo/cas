package org.apereo.cas.authentication.support.password;

import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
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
    AuthenticationPasswordPolicyHandlingStrategy<AuthenticationResponse, PasswordPolicyContext> {

    private final transient WatchableGroovyScriptResource watchableScript;
    private final transient ApplicationContext applicationContext;

    public GroovyPasswordPolicyHandlingStrategy(final Resource groovyScript, final ApplicationContext applicationContext) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
        this.applicationContext = applicationContext;
    }

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final PasswordPolicyContext configuration) {
        val args = new Object[]{response, configuration, LOGGER, applicationContext};
        return watchableScript.execute(args, List.class);
    }
}
