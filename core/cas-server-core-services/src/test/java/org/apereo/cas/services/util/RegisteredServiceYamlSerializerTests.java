package org.apereo.cas.services.util;

import org.apereo.cas.services.RegexRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceYamlSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("RegisteredService")
public class RegisteredServiceYamlSerializerTests {

    @Test
    public void verifyPrinter() {
        val zer = new RegisteredServiceYamlSerializer();
        assertFalse(zer.supports(new File("bad-file")));
        assertFalse(zer.getContentTypes().isEmpty());
        assertNotNull(zer.getJsonFactory());
    }

    @Test
    public void verifyWriter() {
        val zer = new RegisteredServiceYamlSerializer();
        val writer = new StringWriter();
        zer.to(writer, new RegexRegisteredService());
        assertNotNull(zer.from(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8))));
    }

}
