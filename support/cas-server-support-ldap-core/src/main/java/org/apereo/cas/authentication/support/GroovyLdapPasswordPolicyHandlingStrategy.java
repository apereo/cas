package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.util.ScriptingUtils;
import org.ldaptive.auth.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * This is {@link GroovyLdapPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyLdapPasswordPolicyHandlingStrategy implements LdapPasswordPolicyHandlingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyLdapPasswordPolicyHandlingStrategy.class);

    private final Resource groovyResource;

    public GroovyLdapPasswordPolicyHandlingStrategy(final Resource script) {
        this.groovyResource = script;
    }

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final LdapPasswordPolicyConfiguration configuration) {
  
        return ScriptingUtils.executeGroovyScript(groovyResource,
                new Object[]{response, configuration, LOGGER}, List.class);
    }
}
