package org.apereo.cas.util;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ResourceUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Utility")
class ResourceUtilsTests {
    @Test
    void verifyResourceExists() {
        assertThrows(IllegalArgumentException.class, () -> ResourceUtils.getRawResourceFrom(null));
        assertFalse(ResourceUtils.doesResourceExist(new FileSystemResource("invalid.json")));
        val resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.getResource(anyString())).thenThrow(new RuntimeException());
        assertFalse(ResourceUtils.doesResourceExist("bad-resource", resourceLoader));
        assertFalse(ResourceUtils.doesResourceExist(null, resourceLoader));
        assertFalse(ResourceUtils.doesResourceExist("invalid.json"));
        assertTrue(ResourceUtils.doesResourceExist("classpath:valid.json",
            new DefaultResourceLoader(ResourceUtilsTests.class.getClassLoader())));
    }

    @Test
    void verifyResourceOnClasspath() {
        val res = new ClassPathResource("valid.json");
        assertNotNull(ResourceUtils.prepareClasspathResourceIfNeeded(res, false, "valid"));
        assertNull(ResourceUtils.prepareClasspathResourceIfNeeded(null, false, "valid"));
        assertFalse(ResourceUtils.isFile(res.getFilename()));
    }

    @Test
    void verifyPrepare() {
        val url = getClass().getClassLoader().getResource("META-INF/additional-spring-configuration-metadata.json");
        assertNotNull(url);
        val resource = ResourceUtils.prepareClasspathResourceIfNeeded(new UrlResource(url), false, ".*");
        assertNotNull(resource);
    }

    @Test
    void verifyPrepareDir() {
        val url = getClass().getClassLoader().getResource("META-INF");
        assertNotNull(url);
        val resource = ResourceUtils.prepareClasspathResourceIfNeeded(new UrlResource(url), true, "MANIFEST");
        assertNotNull(resource);
    }

    @Test
    void verifyExport() throws Throwable {
        val resourceName = "META-INF/additional-spring-configuration-metadata.json";
        val url = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(url);
        val parent = FileUtils.getTempDirectory();
        assertNull(ResourceUtils.exportClasspathResourceToFile(parent, null));
        assertNotNull(ResourceUtils.exportClasspathResourceToFile(parent, new UrlResource(url)));

        try (val appCtx = new StaticApplicationContext()) {
            appCtx.refresh();
            assertDoesNotThrow(() -> ResourceUtils.exportResources(appCtx, parent,
                List.of("classpath:/" + resourceName)));
        }
        assertTrue(new File(parent, FilenameUtils.getName(resourceName)).exists());
        
        val res = new ClassPathResource("valid.json");
        val file = new File(FileUtils.getTempDirectory(), "/one/two");
        FileUtils.write(new File(file, Objects.requireNonNull(res.getFilename())), "data", StandardCharsets.UTF_8);
        assertNotNull(ResourceUtils.exportClasspathResourceToFile(file, res));
    }

    @Test
    void verifyClasspathResourceDirectory() {
        val url = getClass().getClassLoader().getResource("META-INF/additional-spring-configuration-metadata.json");
        assertNotNull(url);
        val file = new File(url.toExternalForm()).getParentFile();
        assertTrue(ResourceUtils.isJarResource(new ClassPathResource(file.getPath())));
    }

    /**
     * Check that doesResourceExist validates existence of directory.
     */
    @Test
    void verifyResourceExistsDetectsFolder() throws IOException {
        val path = Files.createTempDirectory("castest-");
        assertTrue(ResourceUtils.doesResourceExist(ResourceUtils.getResourceFrom(path.toString())));
        FileUtils.forceDelete(path.toFile());
        val nonFileResourceMissing = ResourceUtils.getRawResourceFrom("classpath:doesnotexist.json");
        assertDoesNotThrow(() -> ResourceUtils.doesResourceExist(nonFileResourceMissing));
        val nonFileExists = ResourceUtils.getRawResourceFrom("classpath:log4j2-test.xml");
        assertDoesNotThrow(() -> ResourceUtils.doesResourceExist(nonFileExists));
    }


}
