package org.jasig.cas.services;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link JpaServiceRegistryDaoImpl}
 * @author battags
 * @since 3.1.0
 */
public class JpaServiceRegistryDaoImplTests  {

    private ServiceRegistryDao  dao;

    @Before
    public void setup() {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaSpringContext.xml");
        this.dao = ctx.getBean("jpaServiceRegistryDao", ServiceRegistryDao.class);
    }

    @Test
    public void verifySaveMethodWithNonExistentServiceAndNoAttributes() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
    }
    
    @Test
    public void verifySaveAttributeReleasePolicy() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        final RegisteredService r2 = this.dao.save(r);
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());

        assertEquals(r, r2);
        assertEquals(r2, r3);
        assertNotNull(r3.getAttributeReleasePolicy());
        assertEquals(r2.getAttributeReleasePolicy(), r3.getAttributeReleasePolicy());
    }

    @Test
    public void verifySaveMethodWithExistingServiceNoAttribute() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        this.dao.save(r);

        final List<RegisteredService> services = this.dao.load();

        final RegisteredService r2 = services.get(0);

        r.setId(r2.getId());
        this.dao.save(r);

        final RegisteredService r3 = this.dao.findServiceById(r.getId());

        assertEquals(r, r2);
        assertEquals(r.getTheme(), r3.getTheme());
    }

    @Test
    public void verifyRegisteredServiceProperties() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setName("test");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setProperties(new HashMap
        );

        this.dao.save(r);

        final List<RegisteredService> services = this.dao.load();

        final RegisteredService r2 = services.get(0);


    }

}
