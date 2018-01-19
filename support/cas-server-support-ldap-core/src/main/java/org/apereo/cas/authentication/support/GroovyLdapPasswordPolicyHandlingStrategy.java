package org.apereo.cas.authentication.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.ScriptingUtils;
import org.ldaptive.auth.AuthenticationResponse;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * This is {@link GroovyLdapPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class GroovyLdapPasswordPolicyHandlingStrategy implements LdapPasswordPolicyHandlingStrategy {
    private final Resource groovyResource;

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final LdapPasswordPolicyConfiguration configuration) {
  
        return ScriptingUtils.executeGroovyScript(groovyResource,
                new Object[]{response, configuration, LOGGER}, List.class);
    }
}
