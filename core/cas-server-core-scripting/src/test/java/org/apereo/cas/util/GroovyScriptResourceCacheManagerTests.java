package org.apereo.cas.util;

import java.nio.file.Files;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptResourceCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptResourceCacheManagerTests {
    @Autowired
    @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
    private ScriptResourceCacheManager<String, ExecutableCompiledScript> cacheManager;

    @Test
    void verifyOperation() throws Throwable {
        val file = Files.createTempFile("scripted", ".groovy").toFile();
        FileUtils.writeStringToFile(file, "println 'hello'", StandardCharsets.UTF_8);

        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val resource = scriptFactory.fromResource(new FileSystemResource(file));

        val id = UUID.randomUUID().toString();
        assertNull(cacheManager.get(id));

        assertFalse(cacheManager.containsKey(id));

        cacheManager.put(id, resource);
        cacheManager.put(id, resource);
        assertTrue(cacheManager.containsKey(id));
        assertNotNull(cacheManager.get(id));

        cacheManager.remove(id);
        assertFalse(cacheManager.containsKey(id));

        cacheManager.put(id, resource);
        cacheManager.clear();
        assertTrue(cacheManager.isEmpty());
        cacheManager.destroy();
    }

}
