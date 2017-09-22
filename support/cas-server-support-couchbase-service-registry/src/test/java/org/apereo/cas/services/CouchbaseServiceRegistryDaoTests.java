package org.apereo.cas.services;


import org.apereo.cas.config.CouchbaseServiceRegistryConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;


/**
 * This is {@link CouchbaseServiceRegistryDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class, CouchbaseServiceRegistryConfiguration.class})
@IfProfileValue(name = "couchbaseEnabled", value = "true")
public class CouchbaseServiceRegistryDaoTests {

    private static final int LOAD_SIZE = 1;

    @Autowired
    @Qualifier("couchbaseServiceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        services.forEach(service -> this.serviceRegistryDao.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        IntStream.range(0, LOAD_SIZE).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        IntStream.range(0, LOAD_SIZE).forEach(i -> assertEquals(list.get(i), results.get(i)));
        IntStream.range(0, LOAD_SIZE).forEach(i -> this.serviceRegistryDao.delete(results.get(i)));
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
