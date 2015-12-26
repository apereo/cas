package org.jasig.cas.support.saml;

import org.apache.commons.io.FileUtils;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.JsonServiceRegistryDao;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

/**
 * The {@link SamlRegisteredServiceTests} handles test cases for {@link org.jasig.cas.support.saml.services.SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SamlRegisteredServiceTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void testSavingSamlService() throws Exception {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("SAMLService");
        service.setServiceId("http://mmoayyed.unicon.net");
        service.setMetadataLocation("/Users/Misagh/Workspace/GitWorkspace/shibboleth-sample-java-sp/src/main/resources/metadata/sp-metadata.xml");
        service.setSignAssertions(true);

        final JsonServiceRegistryDao dao = new JsonServiceRegistryDao(RESOURCE.getFile());
        dao.save(service);
    }

    @Test
    public void checkPattern() {
        final SamlRegisteredService service = new SamlRegisteredService();
        service.setName("SAMLService");
        service.setServiceId("^http://.+");
        service.setMetadataLocation("/Users/Misagh/Workspace/GitWorkspace/shibboleth-sample-java-sp/src/main/resources/metadata/sp-metadata.xml");
        service.setSignAssertions(true);

        final InMemoryServiceRegistryDaoImpl dao = new InMemoryServiceRegistryDaoImpl();
        dao.setRegisteredServices(Arrays.asList(service));
        final DefaultServicesManagerImpl impl = new DefaultServicesManagerImpl(dao);

        final RegisteredService s = impl.findServiceBy(new WebApplicationServiceFactory()
                .createService("http://mmoayyed.unicon.net:8081/sp/saml/SSO"));

        System.out.println(s);

    }

}
