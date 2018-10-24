package org.apereo.cas.util.io;

import org.apereo.cas.category.RestfulApiCategory;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.MockWebServer;

import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link RestfulSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
})
@Category(RestfulApiCategory.class)
@TestPropertySource(properties = {"cas.smsProvider.rest.url=http://localhost:8132"})
public class RestfulSmsSenderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    private MockWebServer webServer;

    @BeforeEach
    public void initialize() {
        this.webServer = new MockWebServer(8132, new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8),
            "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @AfterEach
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifyRestAttributeRepository() {
        assertTrue(communicationsManager.isSmsSenderDefined());
        assertTrue(communicationsManager.sms("CAS", "CAS", "Hello CAS"));
    }
}
