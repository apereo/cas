package org.apereo.cas.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
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
 * Test cases for {@link MongoServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("/mongo-context.xml")
@SpringBootTest
public class MongoServiceRegistryDaoTests {

    @Autowired
    @Qualifier("mongoServiceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        services.forEach(service -> this.serviceRegistryDao.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        IntStream.range(0, 4).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        IntStream.range(0, 4).forEach(i -> assertEquals(list.get(i), results.get(i)));
        IntStream.range(0, 4).forEach(i -> this.serviceRegistryDao.delete(results.get(i)));
        assertTrue(this.serviceRegistryDao.load().isEmpty());
    }

    private static RegisteredService buildService(final int i) {
        final AbstractRegisteredService rs = RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value11");
        values.add("value21");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);

        return rs;
    }


}
