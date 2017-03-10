package org.apereo.cas.services;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is test cases for {@link InMemoryServiceRegistry}.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1.0
 */
public class InMemoryServiceRegistryTests {

    private static final String SERVICE_ID = "service";

    @Test
    public void verifySave() {
        final InMemoryServiceRegistry reg = new InMemoryServiceRegistry();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID);
        assertEquals(reg.save(svc), svc);
    }

    @Test
    public void verifyLoadEmpty() {
        final InMemoryServiceRegistry reg = new InMemoryServiceRegistry();
        assertEquals(reg.load().size(), 0);
    }

    @Test
     public void verifySaveAndLoad() {
        final InMemoryServiceRegistry reg = new InMemoryServiceRegistry();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID);
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.load().size(), 1);
    }

    @Test
    public void verifySaveAndFind() {
        final InMemoryServiceRegistry reg = new InMemoryServiceRegistry();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID);
        assertEquals(reg.save(svc), svc);
        assertEquals(reg.findServiceById(svc.getId()), svc);
    }

    @Test
    public void verifySaveAndDelete() {
        final InMemoryServiceRegistry reg = new InMemoryServiceRegistry();
        final RegisteredService svc = RegisteredServiceTestUtils.getRegisteredService(SERVICE_ID);
        assertEquals(reg.save(svc), svc);
        assertTrue(reg.delete(svc));
        assertEquals(reg.load().size(), 0);
    }
}
