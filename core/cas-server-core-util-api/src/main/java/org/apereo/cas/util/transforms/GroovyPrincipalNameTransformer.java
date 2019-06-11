package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyPrincipalNameTransformer(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public String transform(final String formUserId) {
        return watchableScript.execute(new Object[]{formUserId, LOGGER}, String.class, true);
    }
}
