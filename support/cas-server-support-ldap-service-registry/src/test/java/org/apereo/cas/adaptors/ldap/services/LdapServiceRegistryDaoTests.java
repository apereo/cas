package org.apereo.cas.adaptors.ldap.services;

import com.google.common.collect.Lists;
import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.adaptors.ldap.services.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistryDao;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apereo.cas.adaptors.ldap.AbstractLdapTests.initDirectoryServer;
import static org.junit.Assert.*;

/**
 * Unit test for {@link LdapServiceRegistryDao} class.
 *
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LdapServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"/ldap-context.xml", "/ldap-regservice-test.xml"})
@TestPropertySource(locations = "classpath:/ldapsvc.properties")
public class LdapServiceRegistryDaoTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao dao;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Before
    public void setUp() throws Exception {
        for (final RegisteredService service : this.dao.load()) {
            this.dao.delete(service);
        }
    }

    @Test
    public void verifyEmptyRegistry() {
        assertEquals(0, this.dao.load().size());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.dao.findServiceById(9999991));
    }

    @Test
    public void verifySavingServices() {
        this.dao.save(getRegexRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.dao.save(rs));

        final RegisteredService rs3 = this.dao.findServiceById(rs.getId());
        assertEquals(rs3.getName(), rs.getName());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getProxyPolicy(), rs.getProxyPolicy());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServicesCallback() {
        final OAuthCallbackAuthorizeService r = new OAuthCallbackAuthorizeService();
        r.setName("test345");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthRegisteredServicesCallback() {
        final OAuthCallbackAuthorizeService r = new OAuthCallbackAuthorizeService();
        r.setName("testoauth");
        r.setServiceId(OAuthConstants.CALLBACK_AUTHORIZE_URL_DEFINITION);
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifySavingServiceChangesDn() {
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        final long originalId = rs.getId();
        assertNotNull(rs);
        rs.setId(666);
        assertNotNull(this.dao.save(rs));
        assertNotEquals(rs.getId(), originalId);
    }

    @Test
    public void verifyDeletingSingleService() throws Exception {
        final RegisteredService rs = getRegexRegisteredService();
        final RegisteredService rs2 = getRegexRegisteredService();
        this.dao.save(rs2);
        this.dao.save(rs);
        this.dao.load();
        this.dao.delete(rs2);

        final List<RegisteredService> services = this.dao.load();
        assertEquals(1, services.size());
        assertEquals(services.get(0).getId(), rs.getId());
        assertEquals(services.get(0).getName(), rs.getName());
    }

    @Test
    public void verifyDeletingServices() throws Exception {
        this.dao.save(getRegexRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        for (final RegisteredService registeredService : services) {
            this.dao.delete(registeredService);
        }
        assertEquals(0, this.dao.load().size());
    }

    private static RegisteredService getRegexRegisteredService() {
        final AbstractRegisteredService rs = new RegexRegisteredService();
        rs.setName("Service Name Regex");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
                new ShibbolethCompatiblePersistentIdGenerator("hello")
        ));
        rs.setDescription("Service description");
        rs.setServiceId("^http?://.+");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setDescription("Here is another description");
        rs.setRequiredHandlers(new HashSet<>(Lists.newArrayList("handler1", "handler2")));

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();

        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);

        return rs;
    }


}
