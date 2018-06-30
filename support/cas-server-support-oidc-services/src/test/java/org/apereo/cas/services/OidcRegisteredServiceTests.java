package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.junit.Test;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oidcRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private final ServiceRegistry dao;

    public OidcRegisteredServiceTests() throws Exception {
        this.dao = new JsonServiceRegistry(RESOURCE, false,
                mock(ApplicationEventPublisher.class),
                     new NoOpRegisteredServiceReplicationStrategy(),
                     new DefaultRegisteredServiceResourceNamingStrategy());
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkSaveMethod() {
        final var r = new OidcRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setJwks("file:/tmp/thekeystorehere.jwks");
        r.setSignIdToken(true);
        r.setBypassApprovalPrompt(true);
        final var r2 = this.dao.save(r);
        assertTrue(r2 instanceof OidcRegisteredService);
        this.dao.load();
        final var r3 = this.dao.findServiceById(r2.getId());
        assertTrue(r3 instanceof OidcRegisteredService);
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void verifySerializeAOidcRegisteredServiceToJson() throws IOException {
        final var serviceWritten = new OidcRegisteredService();
        serviceWritten.setName("verifySerializeAOidcRegisteredServiceToJson");
        serviceWritten.setServiceId("testId");
        serviceWritten.setJwks("file:/tmp/thekeystorehere.jwks");
        serviceWritten.setSignIdToken(true);
        serviceWritten.setBypassApprovalPrompt(true);
        serviceWritten.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        final RegisteredService serviceRead = MAPPER.readValue(JSON_FILE, OidcRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
