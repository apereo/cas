package org.apereo.cas.util.transforms;

import module java.base;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

/**
 * A transformer that delegates the transformation to a groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyPrincipalNameTransformer implements PrincipalNameTransformer {
    private final ExecutableCompiledScript watchableScript;

    public GroovyPrincipalNameTransformer(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public String transform(final String formUserId) throws Throwable {
        return watchableScript.execute(new Object[]{formUserId, LOGGER}, String.class, true);
    }
}
