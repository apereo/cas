package org.apereo.cas.authentication.support.password;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;

/**
 * This is {@link GroovyPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class GroovyPasswordEncoder extends AbstractPasswordEncoder {

    private final String scriptFile;

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        final Resource resource = ApplicationContextProvider.getResourceLoader().getResource(this.scriptFile);
        final Object[] args = {rawPassword, salt, LOGGER, ApplicationContextProvider.getApplicationContext()};
        final byte[] result = ScriptingUtils.executeGroovyScript(resource, args, byte[].class);
        return result;
    }
}
