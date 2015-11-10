package org.jasig.cas.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoServiceRegistryDao}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/mongo-context.xml")
public class MongoServiceRegistryDaoTests {

    @Autowired
    private ServiceRegistryDao serviceRegistryDao;

    @Test
    public void verifySaveAndLoad() {
        final List<RegisteredService> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(buildService(i));
            this.serviceRegistryDao.save(list.get(i));
        }
        final List<RegisteredService> results = this.serviceRegistryDao.load();
        assertEquals(results.size(), list.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(list.get(i), results.get(i));
        }
        for (int i = 0; i < 5; i++) {
            this.serviceRegistryDao.delete(results.get(i));
        }
        assertTrue(this.serviceRegistryDao.load().isEmpty());
    }

    private RegisteredService buildService(final int i) {
        return TestUtils.getRegisteredService("^http://www.serviceid" + i + ".org");
    }
}
