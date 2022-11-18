package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
public class DefaultRegisteredServiceUsernameProviderTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultRegisteredServiceUsernameProvider.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyNoCanonAndEncrypt() {
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
        val id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(), service);
        provider.initialize();
        assertEquals(id, principal.getId().toUpperCase());
    }

    @Test
    public void verifyRegServiceUsernameUpper() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
        val principal = RegisteredServiceTestUtils.getPrincipal("id");
        val id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, principal.getId().toUpperCase());
    }

    @Test
    public void verifyPatternRemoval() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
        provider.setRemovePattern("@.+");
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser@example.org");
        val id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, "CASUSER");
    }

    @Test
    public void verifyScopedUsername() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
        provider.setScope("example.org");
        val principal = RegisteredServiceTestUtils.getPrincipal("id");
        val id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, principal.getId().toUpperCase().concat("@EXAMPLE.ORG"));
    }

    @Test
    public void verifyRegServiceUsername() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        val principal = RegisteredServiceTestUtils.getPrincipal("id");
        val id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getRegisteredService("id"));
        assertEquals(id, principal.getId());
    }

    @Test
    public void verifyEquality() {
        val provider = new DefaultRegisteredServiceUsernameProvider();
        val provider2 = new DefaultRegisteredServiceUsernameProvider();
        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws Exception {
        val providerWritten = new DefaultRegisteredServiceUsernameProvider();
        MAPPER.writeValue(JSON_FILE, providerWritten);
        val providerRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceUsernameProvider.class);
        assertEquals(providerWritten, providerRead);
    }
}
