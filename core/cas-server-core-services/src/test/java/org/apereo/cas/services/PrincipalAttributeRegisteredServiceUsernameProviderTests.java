package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
@SpringBootTestAutoConfigurations
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class PrincipalAttributeRegisteredServiceUsernameProviderTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyUsernameByPrincipalAttributeWithMapping() throws Throwable {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("email");

        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", List.of(mappedAttribute));
        val policy = new ReturnMappedAttributeReleasePolicy()
            .setAllowedAttributes(CollectionUtils.wrap(allowedAttributes));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setAttributeReleasePolicy(policy);

        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("email", List.of("user@example.org"));
        val p = RegisteredServiceTestUtils.getPrincipal("person", principalAttributes);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService("verifyUsernameByPrincipalAttributeWithMapping"))
            .principal(p)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("user@example.org", id);
    }

    @Test
    void verifyUsernameByPrincipalAttributeAsCollection() throws Throwable {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", CollectionUtils.wrap("u1"));
        attrs.put("cn", CollectionUtils.wrap("TheName"));

        val p = RegisteredServiceTestUtils.getPrincipal("person", attrs);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(p)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("TheName", id);
    }

    @Test
    void verifyUsernameByPrincipalAttribute() throws Throwable {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val principal = RegisteredServiceTestUtils.getPrincipal("person", attrs);
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("TheName", id);
    }

    @Test
    void verifyNoAttrRelPolicy() throws Throwable {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val p = RegisteredServiceTestUtils.getPrincipal("person", attrs);

        val service = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        service.setAttributeReleasePolicy(null);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(p)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals("TheName", id);
    }

    @Test
    void verifyDisabledService() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val principal = RegisteredServiceTestUtils.getPrincipal("person", attrs);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        registeredService.setAttributeReleasePolicy(null);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        assertThrows(UnauthorizedServiceException.class, () -> provider.resolveUsername(usernameContext));
    }

    @Test
    void verifyUsernameByPrincipalAttributeNotFound() throws Throwable {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));

        val p = RegisteredServiceTestUtils.getPrincipal("person", attrs);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(p)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals(id, p.getId());
    }

    @Test
    void verifyUsernameUndefined() throws Throwable {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider();
        val p = RegisteredServiceTestUtils.getPrincipal("person");
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"))
            .service(RegisteredServiceTestUtils.getService("usernameAttributeProviderService"))
            .principal(p)
            .applicationContext(applicationContext)
            .build();
        val id = provider.resolveUsername(usernameContext);
        assertEquals(id, p.getId());
    }

    @Test
    void verifyEquality() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        val provider2 = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        assertEquals(provider, provider2);
    }

    @Test
    void verifySerializeAPrincipalAttributeRegisteredServiceUsernameProviderToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val providerWritten = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        MAPPER.writeValue(jsonFile, providerWritten);
        val providerRead = MAPPER.readValue(jsonFile, PrincipalAttributeRegisteredServiceUsernameProvider.class);
        assertEquals(providerWritten, providerRead);
    }
}
