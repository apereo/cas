package org.apereo.cas.util;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ResourceUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Utility")
public class ResourceUtilsTests {
    @Test
    public void verifyResourceExists() {
        assertThrows(IllegalArgumentException.class, () -> ResourceUtils.getRawResourceFrom(null));
        assertFalse(ResourceUtils.doesResourceExist(new FileSystemResource("invalid.json")));
        val resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(anyString())).thenThrow(new RuntimeException());
        assertFalse(ResourceUtils.doesResourceExist("bad-resource", resourceLoader));
        assertFalse(ResourceUtils.doesResourceExist("invalid.json"));
        assertTrue(ResourceUtils.doesResourceExist("classpath:valid.json",
            new DefaultResourceLoader(ResourceUtilsTests.class.getClassLoader())));
    }

    @Test
    public void verifyResourceOnClasspath() {
        val res = new ClassPathResource("valid.json");
        assertNotNull(ResourceUtils.prepareClasspathResourceIfNeeded(res, false, "valid"));
        assertNull(ResourceUtils.prepareClasspathResourceIfNeeded(null, false, "valid"));
        assertFalse(ResourceUtils.isFile(res.getFilename()));
    }

    @Test
    public void verifyPrepare() {
        val url = getClass().getClassLoader().getResource("META-INF/additional-spring-configuration-metadata.json");
        assertNotNull(url);
        val resource = ResourceUtils.prepareClasspathResourceIfNeeded(new UrlResource(url), false, ".*");
        assertNotNull(resource);
    }

    @Test
    public void verifyPrepareDir() {
        val url = getClass().getClassLoader().getResource("META-INF");
        assertNotNull(url);
        val resource = ResourceUtils.prepareClasspathResourceIfNeeded(new UrlResource(url), true, "MANIFEST");
        assertNotNull(resource);
    }

    @Test
    public void verifyExport() throws Exception {
        val url = getClass().getClassLoader().getResource("META-INF/additional-spring-configuration-metadata.json");
        assertNotNull(url);
        val parent = FileUtils.getTempDirectory();
        assertNull(ResourceUtils.exportClasspathResourceToFile(parent, null));
        assertNotNull(ResourceUtils.exportClasspathResourceToFile(parent, new UrlResource(url)));

        val res = new ClassPathResource("valid.json");
        val file = new File(FileUtils.getTempDirectory(), "/one/two");
        FileUtils.write(new File(file, res.getFilename()), "data", StandardCharsets.UTF_8);
        assertNotNull(ResourceUtils.exportClasspathResourceToFile(file, res));
    }
}
