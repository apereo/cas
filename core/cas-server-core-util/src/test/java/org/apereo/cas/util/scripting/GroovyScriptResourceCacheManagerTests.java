package org.apereo.cas.util.scripting;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptResourceCacheManagerTests {
    @Autowired
    @Qualifier(ScriptResourceCacheManager.BEAN_NAME)
    private ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> cacheManager;

    @Test
    void verifyOperation() throws Throwable {
        val file = File.createTempFile("scripted", ".groovy");
        FileUtils.writeStringToFile(file, "println 'hello'", StandardCharsets.UTF_8);
        val resource = new WatchableGroovyScriptResource(new FileSystemResource(file));

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
