package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ReturnEnvironmentAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("AttributeRelease")
@ActiveProfiles("production")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnEnvironmentAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new ReturnEnvironmentAttributeReleasePolicy();
        policyWritten.setEnvironmentVariables(CollectionUtils.wrap("HOME", "HOME"));
        policyWritten.setSystemProperties(CollectionUtils.wrap("KEY", "KEY1"));
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, ReturnEnvironmentAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyReleaseRules() throws Throwable {
        System.setProperty("MYKEY", UUID.randomUUID().toString());

        val policy = new ReturnEnvironmentAttributeReleasePolicy();
        policy.setEnvironmentVariables(CollectionUtils.wrap("JAVA_OPTS", "JAVA"));
        policy.setSystemProperties(CollectionUtils.wrap("MYKEY", "KEY"));

        val principal = CoreAuthenticationTestUtils.getPrincipal();
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(3, results.size());
        assertTrue(results.containsKey("JAVA"));
        assertTrue(results.containsKey("KEY"));
        assertTrue(results.containsKey("applicationProfiles"));
    }

}
