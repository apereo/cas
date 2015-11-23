package org.jasig.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link org.jasig.cas.services.InMemoryServiceRegistryDaoImpl}.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class InMemoryServiceRegistryDaoImplTests {

    @Test
    public void verifySave()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = org.jasig.cas.services.TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
    }

    @Test
    public void verifyLoadEmpty() {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        assertEquals(reg.load().size(), 0);
    }

    @Test
     public void verifySaveAndLoad()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = org.jasig.cas.services.TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.load().size(), 1);
    }

    @Test
    public void verifySaveAndFind()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = org.jasig.cas.services.TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.findServiceById(svc.getId()), svc);
    }

    @Test
    public void verifySaveAndDelete()  {
        final InMemoryServiceRegistryDaoImpl reg = new InMemoryServiceRegistryDaoImpl();
        final RegisteredService svc = org.jasig.cas.services.TestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertTrue(reg.delete(svc));
        assertEquals(reg.load().size(), 0);
    }
}
