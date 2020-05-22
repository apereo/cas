package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.io.WatcherService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link SamlRegisteredServiceTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("SAML")
public class SamlRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "samlRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");
    private static final String SAML_SERVICE = "SAMLService";
    private static final String METADATA_LOCATION = "classpath:/metadata/idp-metadata.xml";

    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void verifySavingSamlService() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);

        val dao = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx, new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        dao.save(service);
        dao.load();
    }

    @Test
    public void verifySavingInCommonSamlService() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);
        val policy = new InCommonRSAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.setPolicies(Arrays.asList(policy, new DenyAllAttributeReleasePolicy()));
        service.setAttributeReleasePolicy(chain);

        val dao = new JsonServiceRegistry(new FileSystemResource(FileUtils.getTempDirectory()), WatcherService.noOp(),
            appCtx, new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        dao.save(service);
        dao.load();
    }

    @Test
    public void checkPattern() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("^http://.+");
        service.setMetadataLocation(METADATA_LOCATION);
        val dao = new InMemoryServiceRegistry(appCtx, List.of(service), new ArrayList<>());
        val impl = new DefaultServicesManager(dao, appCtx, new HashSet<>());
        impl.load();

        val s = impl.findServiceBy(new WebApplicationServiceFactory()
            .createService("http://mmoayyed.unicon.net:8081/sp/saml/SSO"));
        assertNotNull(s);
    }

    @Test
    public void verifySerializeAReturnMappedAttributeReleasePolicyToJson() throws IOException {
        val serviceWritten = new SamlRegisteredService();
        serviceWritten.setName(SAML_SERVICE);
        serviceWritten.setServiceId("http://mmoayyed.unicon.net");
        serviceWritten.setMetadataLocation(METADATA_LOCATION);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, SamlRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
