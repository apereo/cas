package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.nio.file.Files;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceUsernameProviderTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyNoCanonAndEncrypt() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        val beanFactory = applicationContext.getBeanFactory();
        val cipher = RegisteredServiceCipherExecutor.noOp();
        beanFactory.initializeBean(cipher, RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME);
        beanFactory.autowireBean(cipher);
        beanFactory.registerSingleton(RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME, cipher);
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);

        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(null);
        provider.setEncryptUsername(true);
        val principal = RegisteredServiceTestUtils.getPrincipal("ID");
        val service = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(service)
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();

        val id = provider.resolveUsername(usernameContext);
        provider.initialize();
        assertEquals(id, principal.getId().toUpperCase(Locale.ENGLISH));
    }

    @Test
    void verifyRegServiceUsernameUpper() throws Throwable {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode("UPPER");
        val principal = RegisteredServiceTestUtils.getPrincipal("id");

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals(id, principal.getId().toUpperCase(Locale.ENGLISH));
    }

    @Test
    void verifyPatternRemoval() throws Throwable {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode("UPPER");
        provider.setRemovePattern("@.+");
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser@example.org");

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("CASUSER", id);
    }

    @Test
    void verifyScopedUsername() throws Throwable {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode("UPPER");
        provider.setScope("example.org");
        val principal = RegisteredServiceTestUtils.getPrincipal("id");

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals(id, principal.getId().toUpperCase(Locale.ENGLISH).concat("@EXAMPLE.ORG"));
    }

    @Test
    void verifyRegServiceUsername() throws Throwable {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        val principal = RegisteredServiceTestUtils.getPrincipal("id");

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("id"))
            .service(RegisteredServiceTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals(id, principal.getId());
    }

    @Test
    void verifyEquality() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        val provider2 = new DefaultRegisteredServiceUsernameProvider();
        assertEquals(provider, provider2);
    }

    @Test
    void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val providerWritten = new DefaultRegisteredServiceUsernameProvider();
        MAPPER.writeValue(jsonFile, providerWritten);
        val providerRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceUsernameProvider.class);
        assertEquals(providerWritten, providerRead);
    }
}
