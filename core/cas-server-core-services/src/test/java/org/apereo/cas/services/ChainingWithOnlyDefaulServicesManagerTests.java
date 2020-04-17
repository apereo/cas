package org.apereo.cas.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingWithOnlyDefaulServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class ChainingWithOnlyDefaulServicesManagerTests extends AbstractServicesManagerTests {

    @Test
    public void verifySupports() {
        var r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        this.servicesManager.save(r);
        assertTrue(this.servicesManager.supports(RegexRegisteredService.class));
        assertTrue(this.servicesManager.supports(r));
        assertTrue(this.servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }

    @Test
    public void verifySaveWithDomains() {
        var svc = new RegexRegisteredService();
        svc.setId(100);
        svc.setName("domainService2");
        svc.setServiceId("https://www.example.com/two");
        assertNotNull(servicesManager.save(svc, false));
        var chain = DomainAwareServicesManager.class.cast(this.servicesManager);
        assertTrue(chain.getDomains().count() == 0);
        assertTrue(chain.getServicesForDomain("example.org").isEmpty());
    }
}
