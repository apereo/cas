package org.apereo.cas.scripting;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ScriptEngineManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 * @deprecated 6.2
 */
@Tag("Groovy")
@Deprecated(since = "6.2.0")
public class ScriptEngineManagerTests {
    private static void runAttributeFilterInternallyFor(final String s) {
        val filter = new ScriptedRegisteredServiceAttributeReleasePolicy(s);
        val principal = CoreAuthenticationTestUtils.getPrincipal("cas", Collections.singletonMap("attribute", List.of("value")));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val attrs = filter.getAttributes(releasePolicyContext);
        assertEquals(attrs.size(), principal.getAttributes().size());
    }

    private static ScriptEngine getEngineNameFor(final String name) {
        val engineName = ScriptingUtils.getScriptEngineName(name);
        assertNotNull(engineName);
        return new ScriptEngineManager().getEngineByName(engineName);
    }

    @Test
    public void verifyEngineNames() {
        assertNotNull(getEngineNameFor("script.py"));
        assertNotNull(getEngineNameFor("script.groovy"));
        assertNotNull(getEngineNameFor("script.js"));
    }

    @Test
    public void verifyPythonAttributeFilter() {
        runAttributeFilterInternallyFor("classpath:attributefilter.py");
    }

    @Test
    public void verifyGroovyAttributeFilter() {
        runAttributeFilterInternallyFor("classpath:attributefilter.groovy");
    }
}
