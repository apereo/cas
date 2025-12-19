package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
class AnonymousRegisteredServiceUsernameAttributeProviderTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String CASROX = "casrox";

    @Test
    void verifyPrincipalResolution() throws Throwable {
        val provider = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val service = mock(Service.class);
        when(service.getId()).thenReturn("id");
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("uid");

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(RegisteredServiceTestUtils.getRegisteredService("id"))
            .service(service)
            .principal(principal)
            .applicationContext(applicationContext)
            .build();

        val id = provider.resolveUsername(usernameContext);
        assertNotNull(id);
    }

    @Test
    void verifyEquality() {
        val provider = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));
        val provider2 = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));

        assertEquals(provider, provider2);
    }

    @Test
    void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val providerWritten = new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator(CASROX));
        MAPPER.writeValue(jsonFile, providerWritten);
        val providerRead = MAPPER.readValue(jsonFile, AnonymousRegisteredServiceUsernameAttributeProvider.class);
        assertEquals(providerWritten, providerRead);
    }

    @Test
    void verifyGeneratedIdsMatch() throws Throwable {
        val salt = "nJ+G!VgGt=E2xCJp@Kb+qjEjE4R2db7NEW!9ofjMNas2Tq3h5h!nCJxc3Sr#kv=7JwU?#MN=7e+r!wpcMw5RF42G8J"
                   + "8tNkGp4g4rFZ#RnNECL@wZX5=yia+KPEwwq#CA9EM38=ZkjK2mzv6oczCVC!m8k!=6@!MW@xTMYH8eSV@7yc24Bz6NUstzbTWH3pnGojZm7pW8N"
                   + "wjLypvZKqhn7agai295kFBhMmpS\n9Jz9+jhVkJfFjA32GiTkZ5hvYiFG104xWnMbHk7TsGrfw%tvACAs=f3C";
        val gen = new ShibbolethCompatiblePersistentIdGenerator(salt);
        gen.setAttribute("employeeId");
        val provider = new AnonymousRegisteredServiceUsernameAttributeProvider(gen);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService("https://cas.example.org/app"))
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("anyuser",
                CollectionUtils.wrap("employeeId", List.of("T911327"))))
            .build();
        val result = provider.resolveUsername(usernameContext);
        assertEquals("ujWTRNKPPso8S+4geOvcOZtv778=", result);
    }

    @Test
    void verifyGeneratedIdsMatchMultiValuedAttribute() throws Throwable {
        val salt = "whydontyoustringmealong";
        val gen = new ShibbolethCompatiblePersistentIdGenerator(salt);
        gen.setAttribute("uid");
        val provider = new AnonymousRegisteredServiceUsernameAttributeProvider(gen);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("anyuser",
                CollectionUtils.wrap("uid", CollectionUtils.wrap("obegon"))))
            .build();
        val result = provider.resolveUsername(usernameContext);
        assertEquals("lykoGRE9QbbrsEBlHJVEz0U8AJ0=", result);
    }
}
