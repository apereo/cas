package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.DynamoDbServiceRegistryConfiguration;
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
import org.springframework.test.context.TestPropertySource;

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
@SpringBootTest(classes = {DynamoDbServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class})
@TestPropertySource(locations = "classpath:/dynamodb-serviceregistry.properties")
@Slf4j
@RunWith(ConditionalSpringRunner.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class DynamoDbServiceRegistryTests {
    @Autowired
    @Qualifier("serviceRegistry")
    private ServiceRegistry serviceRegistry;

    static {
        System.setProperty("aws.accessKeyId", "AKIAIPPIGGUNIO74C63Z");
        System.setProperty("aws.secretKey", "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");
    }

    @Before
    public void setUp() {
        final List<RegisteredService> services = this.serviceRegistry.load();
        services.forEach(service -> this.serviceRegistry.delete(service));
    }

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> {
            list.add(buildService(i));
            this.serviceRegistry.save(list.get(i));
        });
        final List<RegisteredService> results = this.serviceRegistry.load();
        assertEquals(results.size(), list.size());
        IntStream.range(0, 10).forEach(i -> list.contains(results.get(i)));
        IntStream.range(0, 10).forEach(i -> this.serviceRegistry.delete(results.get(i)));
        assertTrue(this.serviceRegistry.load().isEmpty());
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
