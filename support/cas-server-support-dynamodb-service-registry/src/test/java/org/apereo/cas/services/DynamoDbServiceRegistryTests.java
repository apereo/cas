package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.DynamoDbServiceRegistryConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.IfProfileValue;
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
 * This is {@link DynamoDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@IfProfileValue(name = "dynamoDbEnabled", value = "true")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DynamoDbServiceRegistryConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        RefreshAutoConfiguration.class})
@TestPropertySource(properties = {"cas.serviceRegistry.dynamoDb.endpoint=http://localhost:8000",
        "cas.serviceRegistry.dynamoDb.credentialAccessKey=AKIALUS4ZCYABQ",
        "cas.serviceRegistry.dynamoDb.dropTablesOnStartup=true",
        "cas.serviceRegistry.dynamoDb.credentialSecretKey=obZx92Un8zu+D1zTkJOFfZ"})
public class DynamoDbServiceRegistryTests {
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
        IntStream.range(0, 10).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        IntStream.range(0, 10).forEach(i -> list.contains(results.get(i)));
        IntStream.range(0, 10).forEach(i -> this.serviceRegistryDao.delete(results.get(i)));
        assertTrue(this.serviceRegistryDao.load().isEmpty());
    }

    private static RegisteredService buildService(final int i) {
        final AbstractRegisteredService rs = RegisteredServiceTestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
        rs.setEvaluationOrder(i);
        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();
        final Set<String> values = new HashSet<>();
        values.add("value111");
        values.add("value211");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);
        return rs;
    }

}
