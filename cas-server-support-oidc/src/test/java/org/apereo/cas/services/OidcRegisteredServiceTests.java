package org.apereo.cas.services;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.JsonServiceRegistryDao;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcRegisteredServiceTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private ServiceRegistryDao dao;

    public OidcRegisteredServiceTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE);
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }
    
    @Test
    public void checkSaveMethod() {
        final OidcRegisteredService r = new OidcRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setJwks("file:/etc/cas/thekeystorehere.jwks");
        r.setSignIdToken(true);
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof OidcRegisteredService);
        this.dao.load();
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertTrue(r3 instanceof OidcRegisteredService);
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }
}
