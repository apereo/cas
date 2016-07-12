package org.jasig.cas.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/mongo-context.xml")
public class MongoServiceRegistryDaoTests {

    @Autowired
    private ServiceRegistryDao serviceRegistryDao;

    @Before
    public void setup() {
        cleanAll(this.serviceRegistryDao);
    }

    private void cleanAll(final ServiceRegistryDao dao) {
        final List<RegisteredService> services = dao.load();
        for (final RegisteredService service : services) {
            dao.delete(service);
        }
    }

    @Test
    public void verifyEmbeddedConfigurationContext() {
        final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        final Properties properties = new Properties();
        properties.setProperty("mongodb.host", "ds061954.mongolab.com");
        properties.setProperty("mongodb.port", "61954");
        properties.setProperty("mongodb.userId", "casuser");
        properties.setProperty("mongodb.userPassword", "Mellon");
        properties.setProperty("cas.service.registry.mongo.db", "jasigcas");
        configurer.setProperties(properties);

        final FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(
                new String[]{"src/main/resources/META-INF/spring/mongo-services-context.xml"}, false);
        ctx.getBeanFactoryPostProcessors().add(configurer);
        ctx.refresh();

        final MongoServiceRegistryDao dao = new MongoServiceRegistryDao();
        dao.setMongoTemplate(ctx.getBean("mongoTemplate", MongoTemplate.class));
        cleanAll(dao);
        assertTrue(dao.load().isEmpty());
        saveAndLoad(dao);
    }

    @Test
    public void verifySaveAndLoad() {
        saveAndLoad(this.serviceRegistryDao);
    }

    private void saveAndLoad(final ServiceRegistryDao dao) {
        final Set<RegisteredService> list = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            final RegisteredService svc = buildService(i);
            list.add(svc);
            dao.save(svc);
        }
        final List<RegisteredService> results = dao.load();
        assertEquals(results.size(), list.size());
        for (int i = 0; i < 5; i++) {
            dao.delete(results.get(i));
        }
        assertTrue(dao.load().isEmpty());
    }

    private static RegisteredService buildService(final int i) {
        final AbstractRegisteredService rs = TestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");

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
