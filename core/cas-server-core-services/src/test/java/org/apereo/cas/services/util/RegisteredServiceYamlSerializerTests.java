package org.apereo.cas.services.util;

import org.apereo.cas.services.CasRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

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
class RegisteredServiceYamlSerializerTests {

    @Test
    void verifyPrinter() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val zer = new RegisteredServiceYamlSerializer(appCtx);
        assertFalse(zer.supports(new File("bad-file")));
        assertFalse(zer.getContentTypes().isEmpty());
        assertNotNull(zer.getJsonFactory());
    }

    @Test
    void verifyWriter() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val zer = new RegisteredServiceYamlSerializer(appCtx);
        val writer = new StringWriter();
        zer.to(writer, new CasRegisteredService());
        assertNotNull(zer.from(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8))));
    }

}
