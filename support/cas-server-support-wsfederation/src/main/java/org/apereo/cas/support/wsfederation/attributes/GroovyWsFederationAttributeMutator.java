package org.apereo.cas.support.wsfederation.attributes;

import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * This is {@link GroovyWsFederationAttributeMutator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyWsFederationAttributeMutator implements WsFederationAttributeMutator {
    private static final long serialVersionUID = -3864465057274774578L;

    private final transient Resource groovyScript;

    @Override
    public Map<String, List<Object>> modifyAttributes(final Map<String, List<Object>> attributes) {
        val map = ScriptingUtils.executeGroovyScript(this.groovyScript, new Object[]{attributes, LOGGER}, Map.class, true);
        LOGGER.debug("Attributes mutated by [{}] are calculated as [{}]", getClass().getSimpleName(), map);
        return map;
    }
}
