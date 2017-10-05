package org.apereo.cas.support.saml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
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

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void verifySavingSamlService() throws Exception {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);

        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
        dao.save(service);
        dao.load();
    }

    @Test
    public void verifySavingInCommonSamlService() throws Exception {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation(METADATA_LOCATION);
        final InCommonRSAttributeReleasePolicy policy = new InCommonRSAttributeReleasePolicy();
        final ChainingAttributeReleasePolicy chain = new ChainingAttributeReleasePolicy();
        chain.setPolicies(Arrays.asList(policy, new DenyAllAttributeReleasePolicy()));
        service.setAttributeReleasePolicy(chain);

        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
        dao.save(service);
        dao.load();
    }

    @Test
    public void checkPattern() {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName(SAML_SERVICE);
        service.setServiceId("^http://.+");
        service.setMetadataLocation(METADATA_LOCATION);

        final InMemoryServiceRegistry dao = new InMemoryServiceRegistry();
        dao.setRegisteredServices(Collections.singletonList(service));
        final DefaultServicesManager impl = new DefaultServicesManager(dao, mock(ApplicationEventPublisher.class));
        impl.load();

        final RegisteredService s = impl.findServiceBy(new WebApplicationServiceFactory()
                .createService("http://mmoayyed.unicon.net:8081/sp/saml/SSO"));
        assertNotNull(s);
    }

    @Test
    public void verifySerializeAReturnMappedAttributeReleasePolicyToJson() throws IOException {
        final SamlRegisteredService serviceWritten = new SamlRegisteredService();
        serviceWritten.setName(SAML_SERVICE);
        serviceWritten.setServiceId("http://mmoayyed.unicon.net");
        serviceWritten.setMetadataLocation(METADATA_LOCATION);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        final RegisteredService serviceRead = MAPPER.readValue(JSON_FILE, SamlRegisteredService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
