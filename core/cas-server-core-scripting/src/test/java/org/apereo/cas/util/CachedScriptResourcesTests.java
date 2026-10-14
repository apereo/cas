package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.scripting.CachedScriptResources;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachedScriptResourcesTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasCoreScriptingAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CachedScriptResourcesTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOneScriptPerLocation() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val file = Files.createTempFile("cached", ".groovy").toFile();
        FileUtils.writeStringToFile(file, "return 'hello'", StandardCharsets.UTF_8);

        val first = CachedScriptResources.fromResource(new FileSystemResource(file));
        val second = CachedScriptResources.fromResource(new FileSystemResource(file));
        assertSame(first, second);
        assertNotSame(first, CachedScriptResources.fromResource(new FileSystemResource(file), false));
    }

    @Test
    void verifyMissingScriptIsNotCached() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val missing = new FileSystemResource(new File(FileUtils.getTempDirectory(), UUID.randomUUID() + ".groovy"));
        assertNotSame(CachedScriptResources.fromResource(missing), CachedScriptResources.fromResource(missing));
    }

    @Test
    void verifyInlineScriptCompiledOnce() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val script = "return '%s'".formatted(UUID.randomUUID());
        assertSame(CachedScriptResources.fromInlineScript(script), CachedScriptResources.fromInlineScript(script));
        assertNotSame(CachedScriptResources.fromInlineScript(script),
            CachedScriptResources.fromInlineScript(script + " // other"));
    }

    @Test
    void verifyClosedScriptIsResolvedAgain() throws Throwable {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val file = Files.createTempFile("closed", ".groovy").toFile();
        FileUtils.writeStringToFile(file, "return 'hello'", StandardCharsets.UTF_8);

        val first = CachedScriptResources.fromResource(new FileSystemResource(file));
        assertFalse(first.isClosed());
        first.close();
        assertTrue(first.isClosed());
        val second = CachedScriptResources.fromResource(new FileSystemResource(file));
        assertNotSame(first, second);
        assertFalse(second.isClosed());
        assertSame(second, CachedScriptResources.fromResource(new FileSystemResource(file)));
    }
}
