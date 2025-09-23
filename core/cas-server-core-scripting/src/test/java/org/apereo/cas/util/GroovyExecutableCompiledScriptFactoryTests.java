package org.apereo.cas.util;

import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyExecutableCompiledScriptFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyExecutableCompiledScriptFactoryTests {

    @Autowired
    @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
    private ScriptResourceCacheManager<String, ExecutableCompiledScript> scriptResourceCacheManager;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(scriptResourceCacheManager);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        assertNotNull(scriptFactory);
        try (val script = scriptFactory.fromScript("return 1L")) {
            val result = script.execute(ArrayUtils.EMPTY_OBJECT_ARRAY, Long.class);
            assertEquals(1, result);
        }
    }
}
