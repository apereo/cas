package org.jasig.cas.services;


import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.services.OAuthCallbackAuthorizeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * This is {@link MongoServiceRegistryDaoCloudTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/mongo-cloudtest-context.xml")
public class MongoServiceRegistryDaoCloudTests {

    @Autowired
    @Qualifier("serviceRegistryDao")
    private ServiceRegistryDao serviceRegistryDao;


    @Before
    public void clean() {
        final List<RegisteredService> services = this.serviceRegistryDao.load();
        for (final RegisteredService service : services) {
            this.serviceRegistryDao.delete(service);
        }
    }

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

    @Test
    public void verifyOauthService() {
        final OAuthCallbackAuthorizeService service = new OAuthCallbackAuthorizeService();
        service.setServiceId(OAuthConstants.ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE);
        this.serviceRegistryDao.save(service);
    }

    @After
    public void after() {
        clean();
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
