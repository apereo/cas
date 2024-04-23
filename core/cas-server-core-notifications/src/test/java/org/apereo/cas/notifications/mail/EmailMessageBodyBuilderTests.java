package org.apereo.cas.notifications.mail;

import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Locale;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EmailMessageBodyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Mail")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class EmailMessageBodyBuilderTests {

    @Test
    void verifyNoBody() throws Throwable {
        val props = new EmailProperties();
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.GERMAN))
            .parameters(CollectionUtils.wrap("firstname", "Bob"))
            .build();
        val result = results.get();
        assertTrue(result.isBlank());
    }

    @Test
    void verifyLocalizedFileFound() throws Throwable {
        val props = new EmailProperties().setText("classpath:/EmailTemplate.html");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.GERMAN))
            .parameters(CollectionUtils.wrap("firstname", "Bob"))
            .build();
        val result = results.get();
        assertNotNull(result);
        assertTrue(result.contains("Hallo Bob! Dies ist eine E-Mail-Nachricht"));
    }

    @Test
    void verifyLocalizedFileNotFound() throws Throwable {
        val props = new EmailProperties().setText("classpath:/EmailTemplate.html");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.JAPAN))
            .parameters(CollectionUtils.wrap("firstname", "Bob"))
            .build();
        val result = results.get();
        assertNotNull(result);
        assertTrue(result.contains("Hello, World! Bob"));
    }

    @Test
    void verifyOperation() throws Throwable {
        val props = new EmailProperties().setText("${key1}, ${key2}");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.ITALIAN))
            .parameters(CollectionUtils.wrap("key1", "Hello"))
            .build()
            .addParameter("key2", "World");
        val result = results.get();
        assertEquals("Hello, World", result);
    }

    @Test
    void verifyTemplateOperation() throws Throwable {
        val props = new EmailProperties().setText("classpath:/GroovyEmailTemplate.gtemplate");

        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.GERMAN))
            .parameters(CollectionUtils.wrap("firstname", "Bob",
                "lastname", "Smith", "accepted", true,
                "title", "Advanced Title"))
            .build();
        val result = results.get();
        assertNotNull(result);
        assertTrue(result.startsWith("Dear Bob Smith,"));
    }

    @Test
    void verifyInlineGroovyOperation() throws Throwable {
        val props = new EmailProperties().setText("groovy { key + ', ' + key2 }");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.CANADA))
            .parameters(CollectionUtils.wrap("key", "Hello"))
            .build()
            .addParameter("key2", "World");
        val result = results.get();
        assertEquals("Hello, World", result);
    }

    @Test
    void verifyGroovyOperation() throws Throwable {
        val props = new EmailProperties().setText("classpath:/GroovyMessageBody.groovy");
        val results = EmailMessageBodyBuilder.builder()
            .properties(props)
            .locale(Optional.of(Locale.CANADA))
            .parameters(CollectionUtils.wrap("key", "Hello"))
            .build()
            .addParameter("key2", "World");
        val result = results.get();
        assertEquals("Hello, World", result);
    }
}
