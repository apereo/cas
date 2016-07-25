package org.apereo.cas.support.saml;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImpl;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.DefaultServicesManagerImpl;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

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

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void verifySavingSamlService() throws Exception {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("SAMLService");
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation("classpath:/sample-idp-metadata.xml");

        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
        dao.save(service);
        dao.load();
    }

    @Test
    public void checkPattern() {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("SAMLService");
        service.setServiceId("^http://.+");
        service.setMetadataLocation("classpath:/sample-idp-metadata.xml");

        final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
        dao.setRegisteredServices(Collections.singletonList(service));
        final DefaultServicesManagerImpl impl = new DefaultServicesManagerImpl(dao);
        impl.load();

        final RegisteredService s = impl.findServiceBy(new WebApplicationServiceFactory()
                .createService("http://mmoayyed.unicon.net:8081/sp/saml/SSO"));
        assertNotNull(s);
    }

}
