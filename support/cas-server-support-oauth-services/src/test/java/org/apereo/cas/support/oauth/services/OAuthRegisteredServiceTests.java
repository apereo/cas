package org.apereo.cas.support.oauth.services;

import module java.base;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("OAuth")
class OAuthRegisteredServiceTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthRegisteredService.json");

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private ServiceRegistry dao;

    @BeforeEach
    void setup() {
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
    void checkSaveMethod() {
        val registeredService = new OAuthRegisteredService();
        registeredService.setName("checkSaveMethod");
        registeredService.setServiceId("testId");
        registeredService.setTheme("theme");
        registeredService.setDescription("description");
        registeredService.setClientId("clientid");
        registeredService.setServiceId("secret");
        registeredService.setBypassApprovalPrompt(true);
        val r2 = this.dao.save(registeredService);
        assertInstanceOf(OAuthRegisteredService.class, r2);
        this.dao.load();
        val r3 = this.dao.findServiceById(r2.getId());
        assertInstanceOf(OAuthRegisteredService.class, r3);
        assertEquals(registeredService, r2);
        assertEquals(r2, r3);
    }

    @Test
    void verifySerializeOAuthRegisteredServiceToJson() {
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
        serviceWritten.setTokenExchangePolicy(new DefaultRegisteredServiceOAuthTokenExchangePolicy());

        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serializer = new RegisteredServiceJsonSerializer(appCtx);
        serializer.to(JSON_FILE, serviceWritten);
        val serviceRead = serializer.from(JSON_FILE);
        assertEquals(serviceWritten, serviceRead);
    }
}
