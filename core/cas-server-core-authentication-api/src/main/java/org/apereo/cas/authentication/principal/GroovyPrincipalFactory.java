package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * Factory to create {@link SimplePrincipal} objects.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GroovyPrincipalFactory extends DefaultPrincipalFactory {
    private static final long serialVersionUID = -3999695695604948495L;
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyPrincipalFactory(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) {
        return watchableScript.execute(new Object[]{id, attributes, LOGGER}, Principal.class);
    }
}
