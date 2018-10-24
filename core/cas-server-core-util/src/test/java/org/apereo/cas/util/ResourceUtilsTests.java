package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;

import static org.junit.Assert.*;

/**
 * This is {@link ResourceUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
public class ResourceUtilsTests {
    @Test
    public void verifyResourceExists() {
        assertFalse(ResourceUtils.doesResourceExist(new FileSystemResource("invalid.json")));
        assertFalse(ResourceUtils.doesResourceExist("invalid.json"));
        assertTrue(ResourceUtils.doesResourceExist("classpath:valid.json", new DefaultResourceLoader(ResourceUtilsTests.class.getClassLoader())));
    }

    @Test
    public void verifyResourceOnClasspath() {
        val res = new ClassPathResource("valid.json");
        assertNotNull(ResourceUtils.prepareClasspathResourceIfNeeded(res, false, "valid"));
        assertNull(ResourceUtils.prepareClasspathResourceIfNeeded(null, false, "valid"));
    }
}
