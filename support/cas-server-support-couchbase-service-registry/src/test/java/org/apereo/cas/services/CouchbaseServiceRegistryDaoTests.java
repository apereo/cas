package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CouchbaseServiceRegistryConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalSpringRunner;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link CouchbaseServiceRegistryDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class, CouchbaseServiceRegistryConfiguration.class})
@Slf4j
@RunWith(ConditionalSpringRunner.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class CouchbaseServiceRegistryDaoTests {

    private static final int LOAD_SIZE = 1;

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        services.forEach(service -> this.serviceRegistryDao.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < LOAD_SIZE; i++) {
            final RegisteredService svc = buildService(i);
            list.add(svc);
            this.serviceRegistryDao.save(svc);
            final RegisteredService svc2 = this.serviceRegistryDao.findServiceById(svc.getId());
            assertNotNull(svc2);
            this.serviceRegistryDao.delete(svc2);
        }
        assertTrue(this.serviceRegistryDao.load().isEmpty());
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
