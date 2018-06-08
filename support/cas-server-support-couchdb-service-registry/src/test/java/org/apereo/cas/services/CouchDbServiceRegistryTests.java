package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.category.CouchDbCategory;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.services.RegisteredServiceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link CouchDbServiceRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "org.ektorp.support.AutoUpdateViewOnChange=true",
        "cas.serviceRegistry.couchDb.username=",
        "cas.serviceRegistry.couchDb.password="
    })
@Slf4j
@Category(CouchDbCategory.class)
public class CouchDbServiceRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final int LOAD_SIZE = 1;

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("couchDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier("serviceRegistryCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("serviceRegistryCouchDbRepository")
    private RegisteredServiceRepository registeredServiceRepository;

    @Before
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        registeredServiceRepository.initStandardDesignDocument();
    }

    @After
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < LOAD_SIZE; i++) {
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
    public void verifyFindByServiceId() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < LOAD_SIZE; i++) {
            final RegisteredService svc = buildService(i);
            list.add(svc);
            this.serviceRegistry.save(svc);
            final RegisteredService svc2 = this.serviceRegistry.findServiceByExactServiceId(svc.getServiceId());
            assertNotNull(svc2);

            this.serviceRegistry.delete(svc2);
        }
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    @Test
    public void verifyFindByServiceName() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < LOAD_SIZE; i++) {
            final RegisteredService svc = buildService(i);
            list.add(svc);
            this.serviceRegistry.save(svc);
            final RegisteredService svc2 = this.serviceRegistry.findServiceByExactServiceName(svc.getName());
            assertNotNull(svc2);

            this.serviceRegistry.delete(svc2);
        }
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    private static RegisteredService buildService(final int i) {
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

}
