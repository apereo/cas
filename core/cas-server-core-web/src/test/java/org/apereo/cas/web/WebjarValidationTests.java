package org.apereo.cas.web;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Webjar paths aren't always consistent from version to version so check that the paths exist in the jars.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Slf4j
public class WebjarValidationTests {

    @Test
    public void verifyValidation() throws IOException {
        val compositePropertySource = new CompositePropertySource("webjars");
        compositePropertySource.addPropertySource(new PropertiesPropertySource("messages",
            PropertiesLoaderUtils.loadProperties(new ClassPathResource("cas_common_messages.properties"))));
        compositePropertySource.addPropertySource(new PropertiesPropertySource("versions",
            PropertiesLoaderUtils.loadProperties(new FileSystemResource("../../gradle.properties"))));
        for (val key : compositePropertySource.getPropertyNames()) {
            if (key.startsWith("webjars.")) {
                val path = compositePropertySource.getProperty(key);
                LOGGER.info("Webjars  {} = {}", key, path);
                val resource = new ClassPathResource("META-INF/resources" + path);
                assertTrue(resource.exists(), "Webjar path bad: " + path);
            }
        }
    }

}
