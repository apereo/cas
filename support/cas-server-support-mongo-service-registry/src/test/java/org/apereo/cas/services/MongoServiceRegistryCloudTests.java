package org.apereo.cas.services;


import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.MongoDbServiceRegistryConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
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
 * This is {@link MongoServiceRegistryCloudTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MongoDbServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/mongoservices.properties"})
@Slf4j
public class MongoServiceRegistryCloudTests {

    @Autowired
    @Qualifier("mongoDbServiceRegistry")
    private ServiceRegistry serviceRegistry;


    @Before
    public void clean() {
        final List<RegisteredService> services = this.serviceRegistry.load();
        services.forEach(service -> this.serviceRegistry.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        IntStream.range(0, 5).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistry.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistry.load();
        assertEquals(results.size(), list.size());
        IntStream.range(0, 5).forEach(i -> assertEquals(list.get(i), results.get(i)));
        IntStream.range(0, 5).forEach(i -> this.serviceRegistry.delete(results.get(i)));
        assertTrue(this.serviceRegistry.load().isEmpty());
    }

    @After
    public void after() {
        clean();
    }

    private static RegisteredService buildService(final int i) {
        final AbstractRegisteredService rs = RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value10");
        values.add("value20");
        property.setValues(values);
        propertyMap.put("field2", property);
        rs.setProperties(propertyMap);
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider());
        
        return rs;
    }
}
