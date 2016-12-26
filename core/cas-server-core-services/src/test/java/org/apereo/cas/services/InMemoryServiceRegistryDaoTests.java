package org.apereo.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link InMemoryServiceRegistryDao}.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class InMemoryServiceRegistryDaoTests {

    @Test
    public void verifySave() {
        final InMemoryServiceRegistryDao reg = new InMemoryServiceRegistryDao();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
    }

    @Test
    public void verifyLoadEmpty() {
        final InMemoryServiceRegistryDao reg = new InMemoryServiceRegistryDao();
        assertEquals(reg.load().size(), 0);
    }

    @Test
     public void verifySaveAndLoad() {
        final InMemoryServiceRegistryDao reg = new InMemoryServiceRegistryDao();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.load().size(), 1);
    }

    @Test
    public void verifySaveAndFind() {
        final InMemoryServiceRegistryDao reg = new InMemoryServiceRegistryDao();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.findServiceById(svc.getId()), svc);
    }

    @Test
    public void verifySaveAndDelete() {
        final InMemoryServiceRegistryDao reg = new InMemoryServiceRegistryDao();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService("service");
        assertEquals(reg.save(svc), svc);
        assertTrue(reg.delete(svc));
        assertEquals(reg.load().size(), 0);
    }
}
