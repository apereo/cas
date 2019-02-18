package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.JsonServiceRegistry;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The {@link SamlRegisteredServiceTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlRegisteredServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "samlRegisteredService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");
    private static final String SAML_SERVICE = "SAMLService";
    private static final String METADATA_LOCATION = "classpath:/metadata/idp-metadata.xml";

    @BeforeAll
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void verifySavingSamlService() throws Exception {
        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);

        val dao = new JsonServiceRegistry(RESOURCE, false,
            mock(ApplicationEventPublisher.class), new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy());
        dao.save(service);
        dao.load();
    }

    @Test
    public void verifySavingInCommonSamlService() throws Exception {
        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);
        val policy = new InCommonRSAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.setPolicies(Arrays.asList(policy, new DenyAllAttributeReleasePolicy()));
        service.setAttributeReleasePolicy(chain);

        val dao = new JsonServiceRegistry(RESOURCE, false,
            mock(ApplicationEventPublisher.class), new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy());
        dao.save(service);
        dao.load();
    }

    @Test
    public void checkPattern() {
        val service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("^http://.+");
        service.setMetadataLocation(METADATA_LOCATION);
        val dao = new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class), Collections.singletonList(service));
        val impl = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class), new HashSet<>());
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
