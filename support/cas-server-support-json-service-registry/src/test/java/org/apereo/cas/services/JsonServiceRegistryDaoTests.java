package org.apereo.cas.services;

import org.junit.Before;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

/**
 * Handles test cases for {@link JsonServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDaoTests extends AbstractResourceBasedServiceRegistryDaoTests {

    @Before
    public void setup() {
        try {
            this.dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
