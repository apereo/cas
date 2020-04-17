package org.apereo.cas.util.scripting;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WatchableGroovyScriptResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
public class WatchableGroovyScriptResourceTests {

    @Test
    public void verifyOperation() throws Exception {
        val file = File.createTempFile("file", ".groovy");
        FileUtils.writeStringToFile(file, "println 'hello'", StandardCharsets.UTF_8);
        val resource = new WatchableGroovyScriptResource(new FileSystemResource(file));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                resource.execute(ArrayUtils.EMPTY_OBJECT_ARRAY);
            }
        });
        Files.setLastModifiedTime(file.toPath(), FileTime.from(Instant.now()));
        Thread.sleep(5_000);
    }
}
