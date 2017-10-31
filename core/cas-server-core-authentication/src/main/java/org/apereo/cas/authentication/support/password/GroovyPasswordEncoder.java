package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;

/**
 * This is {@link GroovyPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyPasswordEncoder extends AbstractPasswordEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyPasswordEncoder.class);
    
    private final String scriptFile;

    public GroovyPasswordEncoder(final String scriptFile) {
        this.scriptFile = scriptFile;
    }

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        final Resource resource = ApplicationContextProvider.getResourceLoader().getResource(this.scriptFile);
        final Object[] args = {rawPassword, salt, LOGGER, ApplicationContextProvider.getApplicationContext()};
        final byte[] result = ScriptingUtils.executeGroovyScript(resource, args, byte[].class);
        return result;
    }
}
