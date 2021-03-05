package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.WatcherService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("OAuth")
public class OAuthRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthRegisteredService.json");

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private final ServiceRegistry dao;

    public OAuthRegisteredServiceTests() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        this.dao = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx, new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
    }

    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkSaveMethod() {
        val r = new OAuthRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setClientId("clientid");
        r.setServiceId("secret");
        r.setBypassApprovalPrompt(true);
        val r2 = this.dao.save(r);
        assertTrue(r2 instanceof OAuthRegisteredService);
        this.dao.load();
        val r3 = this.dao.findServiceById(r2.getId());
        assertTrue(r3 instanceof OAuthRegisteredService);
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void verifySerializeAOAuthRegisteredServiceToJson() {
        val serviceWritten = new OAuthRegisteredService();
        serviceWritten.setName("checkSaveMethod");
        serviceWritten.setServiceId("testId");
        serviceWritten.setTheme("theme");
        serviceWritten.setDescription("description");
        serviceWritten.setClientId("clientid");
        serviceWritten.setServiceId("secret");
        serviceWritten.setBypassApprovalPrompt(true);
        serviceWritten.setSupportedGrantTypes(CollectionUtils.wrapHashSet("something"));
        serviceWritten.setSupportedResponseTypes(CollectionUtils.wrapHashSet("something"));

        val serializer = new RegisteredServiceJsonSerializer();
        serializer.to(JSON_FILE, serviceWritten);
        val serviceRead = serializer.from(JSON_FILE);
        assertEquals(serviceWritten, serviceRead);
    }
}
