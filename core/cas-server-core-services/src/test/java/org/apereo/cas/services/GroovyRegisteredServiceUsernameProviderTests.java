package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("GroovyServices")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
class GroovyRegisteredServiceUsernameProviderTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyUsernameProvider() throws Throwable {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("classpath:uid.groovy");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("fromscript", id);
    }

    @Test
    void verifyUsernameProviderInline() throws Throwable {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return attributes['uid'] + '123456789' }");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "CAS-System")))
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("CAS-System123456789", id);
    }

    @Test
    void verifyUsernameProviderInlineAsList() throws Throwable {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return attributes['uid'][0] + '123456789' }");
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", List.of("CAS-System"))))
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("CAS-System123456789", id);
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return 'something' }");
        provider.setEncryptUsername(true);
        provider.setCanonicalizationMode("NONE");
        MAPPER.writeValue(jsonFile, provider);
        val repositoryRead = MAPPER.readValue(jsonFile, GroovyRegisteredServiceUsernameProvider.class);
        assertEquals(provider, repositoryRead);
    }

    @Test
    void verifyUsernameProviderInlineWithoutAttribute() throws Throwable {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return attributes['unknown-attribute'][0] + '123456789' }");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser"))
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("casuser", id);
    }
}
