package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("GroovyServices")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class GroovyRegisteredServiceUsernameProviderTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "GroovyRegisteredServiceUsernameProviderTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyUsernameProvider() {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("classpath:uid.groovy");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal())
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("fromscript", id);
    }

    @Test
    public void verifyUsernameProviderInline() {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return attributes['uid'] + '123456789' }");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "CAS-System")))
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("CAS-System123456789", id);
    }

    @Test
    public void verifyUsernameProviderInlineAsList() {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return attributes['uid'][0] + '123456789' }");
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .service(RegisteredServiceTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", List.of("CAS-System"))))
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("CAS-System123456789", id);
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        val provider = new GroovyRegisteredServiceUsernameProvider();
        provider.setGroovyScript("groovy { return 'something' }");
        provider.setEncryptUsername(true);
        provider.setCanonicalizationMode("NONE");
        MAPPER.writeValue(JSON_FILE, provider);
        val repositoryRead = MAPPER.readValue(JSON_FILE, GroovyRegisteredServiceUsernameProvider.class);
        assertEquals(provider, repositoryRead);
    }
}
