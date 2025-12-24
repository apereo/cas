package org.apereo.cas.authentication.support.password;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

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

    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    public GroovyPasswordPolicyHandlingStrategy(final Resource groovyScript, final ApplicationContext applicationContext) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyScript);
        this.applicationContext = applicationContext;
    }

    @Override
    public @Nullable List<MessageDescriptor> handle(@Nullable final AuthenticationResponse response,
                                                    final PasswordPolicyContext configuration) throws Throwable {
        val args = new Object[]{Objects.requireNonNull(response), configuration, LOGGER, applicationContext};
        return watchableScript.execute(args, List.class);
    }
}
