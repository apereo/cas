package org.apereo.cas.services;

import org.junit.Before;

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
            this.dao = new JsonServiceRegistryDao(RESOURCE, false);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
