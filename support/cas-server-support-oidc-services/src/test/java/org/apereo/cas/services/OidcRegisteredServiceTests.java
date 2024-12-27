package org.apereo.cas.services;

import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link OidcRegisteredService}.
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("OIDCServices")
class OidcRegisteredServiceTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private final ServiceRegistry dao;

    OidcRegisteredServiceTests() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        this.dao = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
    }

    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    void checkSaveMethod() {
        val r = new OidcRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setJwks("file:/tmp/thekeystorehere.jwks");
        r.setSignIdToken(true);
        r.setBypassApprovalPrompt(true);
        val r2 = this.dao.save(r);
        assertInstanceOf(OidcRegisteredService.class, r2);
        this.dao.load();
        val r3 = this.dao.findServiceById(r2.getId());
        assertInstanceOf(OidcRegisteredService.class, r3);
        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r.getFriendlyName());
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
