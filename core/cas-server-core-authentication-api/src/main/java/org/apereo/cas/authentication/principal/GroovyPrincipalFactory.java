package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * Factory to create {@link SimplePrincipal} objects.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GroovyPrincipalFactory extends DefaultPrincipalFactory {
    @Serial
    private static final long serialVersionUID = -3999695695604948495L;

    private final ExecutableCompiledScript watchableScript;

    public GroovyPrincipalFactory(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public @Nullable Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) throws Throwable {
        return watchableScript.execute(new Object[]{id, attributes, LOGGER}, Principal.class);
    }
}
