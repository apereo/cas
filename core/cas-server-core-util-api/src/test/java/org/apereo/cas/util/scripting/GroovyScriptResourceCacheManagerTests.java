package org.apereo.cas.util.scripting;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
public class GroovyScriptResourceCacheManagerTests {
    @Test
    public void verifyOperation() throws Exception {
        val file = File.createTempFile("scripted", ".groovy");
        FileUtils.writeStringToFile(file, "println 'hello'", StandardCharsets.UTF_8);
        val resource = new WatchableGroovyScriptResource(new FileSystemResource(file));

        val cache = new GroovyScriptResourceCacheManager();
        
        val id = UUID.randomUUID().toString();
        assertNull(cache.get(id));

        assertFalse(cache.containsKey(id));

        cache.put(id, resource);
        cache.put(id, resource);
        assertTrue(cache.containsKey(id));
        assertNotNull(cache.get(id));

        cache.remove(id);
        assertFalse(cache.containsKey(id));

        cache.put(id, resource);
        cache.clear();
        assertTrue(cache.isEmpty());
        cache.destroy();
    }

}
