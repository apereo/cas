package org.apereo.cas.services.util;

import lombok.SneakyThrows;
import org.apereo.cas.services.RegisteredService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * This is {@link CasAddonsRegisteredServicesJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(JUnit4.class)
public class CasAddonsRegisteredServicesJsonSerializerTests {

    @Test
    public void verifySupports() {
        final CasAddonsRegisteredServicesJsonSerializer s = new CasAddonsRegisteredServicesJsonSerializer();
        assertTrue(s.supports(new File("servicesRegistry.conf")));
    }

    @Test
    public void verifyLoad() {
        final CasAddonsRegisteredServicesJsonSerializer s = new CasAddonsRegisteredServicesJsonSerializer();
        final Collection<RegisteredService> services = s.load(getServiceRegistryResource());
        assertEquals(3, services.size());
    }

    @SneakyThrows
    private InputStream getServiceRegistryResource() {
        final File file = new File("servicesRegistry.conf");
        return new ClassPathResource(file.getPath()).getInputStream();
    }
}
