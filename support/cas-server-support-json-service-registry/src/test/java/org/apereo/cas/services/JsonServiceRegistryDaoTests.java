package org.apereo.cas.services;

import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Handles test cases for {@link JsonServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDaoTests extends AbstractResourceBasedServiceRegistryDaoTests {

    @Before
    public void setUp() {
        try {
            this.dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Test
    public void verifyLegacyServiceDefn() throws Exception {
        final ClassPathResource resource = new ClassPathResource("Legacy-10000003.json");
        final RegisteredServiceJsonSerializer serializer = new RegisteredServiceJsonSerializer();
        final RegisteredService service = serializer.from(resource.getInputStream());
        assertNotNull(service);
    }
}
