package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasJsonServiceRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link OidcRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("OIDCServices")
@SpringBootTestAutoConfigurations
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasJsonServiceRegistryAutoConfiguration.class
}, properties = "cas.service-registry.json.location=classpath:/services")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class OidcRegisteredServiceTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeAll
    @AfterAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    void checkSaveMethod() {
        val registeredService = new OidcRegisteredService();
        registeredService.setName("checkSaveMethod");
        registeredService.setServiceId("testId");
        registeredService.setJwks("file:/tmp/thekeystorehere.jwks");
        registeredService.setSignIdToken(true);
        registeredService.setBypassApprovalPrompt(true);
        val r2 = servicesManager.save(registeredService);
        assertInstanceOf(OidcRegisteredService.class, r2);
        servicesManager.load();
        val r3 = servicesManager.findServiceBy(r2.getId());
        assertInstanceOf(OidcRegisteredService.class, r3);
        assertEquals(registeredService, r2);
        assertEquals(r2, r3);
        assertNotNull(registeredService.getFriendlyName());
    }

    @Test
    void verifySerializeAOidcRegisteredServiceToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val serviceWritten = new OidcRegisteredService();
        serviceWritten.setName("verifySerializeAOidcRegisteredServiceToJson");
        serviceWritten.setServiceId("testId");
        serviceWritten.setJwks("file:/tmp/thekeystorehere.jwks");
        serviceWritten.setSignIdToken(true);
        serviceWritten.setBypassApprovalPrompt(true);
        serviceWritten.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
        MAPPER.writeValue(jsonFile, serviceWritten);
        val serviceRead = MAPPER.readValue(jsonFile, OidcRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }


}
