package org.apereo.cas.entity;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdentityProviderEntityParserTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAML2")
class SamlIdentityProviderEntityParserTests {
    @Test
    void verifyResource() throws Throwable {
        val parser = new SamlIdentityProviderEntityParser(new ClassPathResource("disco-feed.json"));
        assertFalse(parser.getIdentityProviders().isEmpty());
    }

    @Test
    void verifyFile() throws Throwable {
        val file = File.createTempFile("feed", ".json");
        val content = IOUtils.toString(new ClassPathResource("disco-feed.json").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        val parser = new SamlIdentityProviderEntityParser(new FileSystemResource(file));
        assertFalse(parser.getIdentityProviders().isEmpty());
        Files.setLastModifiedTime(file.toPath(), FileTime.from(Instant.now()));
        Thread.sleep(8_000);
        parser.destroy();
    }

    @Test
    void verifyEntity() throws Throwable {
        val entity = new SamlIdentityProviderEntity();
        entity.setEntityID("org.example");
        val parser = new SamlIdentityProviderEntityParser(entity);
        assertEquals(1, parser.getIdentityProviders().size());
    }
}
