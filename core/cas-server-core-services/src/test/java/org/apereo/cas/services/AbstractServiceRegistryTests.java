package org.apereo.cas.services;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

/**
 * Abstracted service registry tests for all implementations.
 * @author Timur Duehr
 * @since 5.3.0
 */
@Slf4j
@Getter
public abstract class AbstractServiceRegistryTests {

    public static final int LOAD_SIZE = 1;
    private static final String SERVICE_ID = "service";

    private ServiceRegistry serviceRegistry;

    @Before
    public void setUp() {
        this.serviceRegistry = this.getNewServiceRegistry();
        this.initializeServiceRegistry();
    }

    /**
     * Abstract method to retrieve a new service registry. Implementing classes
     * return the ServiceRegistry they wish to test.
     *
     * @return the ServiceRegistry we wish to test
     */
    public abstract ServiceRegistry getNewServiceRegistry();

    /**
     * Verify tests start with empty registry
     */
    @Test
    public void verifyEmptyRegistry() {
        assertEquals(0, this.serviceRegistry.load().size());
    }

    @Test
    public void verifySave() {
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID);
        assertEquals(serviceRegistry.save(svc), svc);
    }

    /**
     * Method to test service registry saving and loading a service.
     */
    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < getLoadSize(); i++) {
            final RegisteredService svc = buildService(i);
            list.add(svc);
            this.serviceRegistry.save(svc);
            final RegisteredService svc2 = this.serviceRegistry.findServiceById(svc.getId());
            assertNotNull(svc2);
            this.serviceRegistry.delete(svc2);
        }
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.serviceRegistry.findServiceById(9999991));
    }

    @Test
    public void verifySavingServices() {
        this.serviceRegistry.save(buildService(100));
        List<RegisteredService> services = this.serviceRegistry.load();
        assertEquals(1, services.size());
        this.serviceRegistry.save(buildService(101));
        services = this.serviceRegistry.load();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.serviceRegistry.save(buildService(200));
        final List<RegisteredService> services = this.serviceRegistry.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.serviceRegistry.findServiceById(services.get(0).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.serviceRegistry.save(rs));

        final RegisteredService rs3 = this.serviceRegistry.findServiceById(rs.getId());
        assertEquals(rs3.getName(), rs.getName());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getProxyPolicy(), rs.getProxyPolicy());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
    }

    @Test
    public void verifySamlService() {
        final SamlRegisteredService r = new SamlRegisteredService();
        r.setName("verifySamlService");
        r.setServiceId("Testing");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Map fmt = new HashMap();
        fmt.put("key", "value");
        r.setAttributeNameFormats(fmt);
        r.setMetadataCriteriaDirection("INCLUDE");
        r.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
        r.setMetadataSignatureLocation("location");
        r.setRequiredAuthenticationContextClass("Testing");
        final SamlRegisteredService r2 = (SamlRegisteredService) this.serviceRegistry.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test1456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.serviceRegistry.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyDeletingSingleService() {
        final RegisteredService rs = buildService(300);
        final RegisteredService rs2 = buildService(301);
        this.serviceRegistry.save(rs2);
        this.serviceRegistry.save(rs);
        this.serviceRegistry.load();
        this.serviceRegistry.delete(rs2);

        final List<RegisteredService> services = this.serviceRegistry.load();
        assertEquals(1, services.size());
        assertEquals(services.get(0).getId(), rs.getId());
        assertEquals(services.get(0).getName(), rs.getName());
    }

    @Test
    public void verifyDeletingServices() {
        this.serviceRegistry.save(buildService(400));
        this.serviceRegistry.save(buildService(401));
        final List<RegisteredService> services = this.serviceRegistry.load();
        services.forEach(registeredService -> this.serviceRegistry.delete(registeredService));
        assertEquals(0, this.serviceRegistry.load().size());
    }

    /**
     * Method to mock RegisteredService objects for testing.
     * @param i addition to service name for uniqueness.
     * @return new registered service object
     */
    protected static RegisteredService buildService(final int i) {
        final AbstractRegisteredService rs = RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);
        return rs;
    }

    public int getLoadSize() {
        return LOAD_SIZE;
    }

    /**
     * Method to prepare the service registry for testing. Iplementing classes may override this if more is necessary.
     */
    public void initializeServiceRegistry() {
        this.getServiceRegistry().load().forEach(service -> this.getServiceRegistry().delete(service));
    }
}
