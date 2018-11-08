package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.ScriptingUtils;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * Factory to create {@link SimplePrincipal} objects.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class GroovyPrincipalFactory extends DefaultPrincipalFactory {
    private static final long serialVersionUID = -3999695695604948495L;
    private final transient Resource groovyResource;

    @Override
    public Principal createPrincipal(final String id, final Map<String, Object> attributes) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, new Object[]{id, attributes, LOGGER}, Principal.class, true);
    }
}
