package org.apereo.cas.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Webjar paths aren't always consistent from version to version so check that the paths exist in the jars.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Tag("Simple")
public class WebjarValidationTests {

    @Test
    public void verifyValidation() throws IOException {
        val compositePropertySource = new CompositePropertySource("webjars");
        compositePropertySource.addPropertySource(new PropertiesPropertySource("messages",
            PropertiesLoaderUtils.loadProperties(new ClassPathResource("cas_common_messages.properties"))));
        compositePropertySource.addPropertySource(new PropertiesPropertySource("versions",
            PropertiesLoaderUtils.loadProperties(new FileSystemResource("../../gradle.properties"))));
        Arrays.stream(compositePropertySource.getPropertyNames())
            .filter(key -> key.startsWith("webjars."))
            .map(key -> new ClassPathResource("META-INF/resources" + compositePropertySource.getProperty(key)))
            .forEach(resource -> assertTrue(resource.exists(), () -> "Webjar path bad: " + resource.getPath()));
    }

}
