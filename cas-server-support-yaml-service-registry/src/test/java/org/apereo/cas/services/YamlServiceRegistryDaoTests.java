package org.apereo.cas.services;


import org.junit.Before;

/**
 * Test cases for {@link YamlServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YamlServiceRegistryDaoTests extends AbstractResourceBasedServiceRegistryDaoTests {

    @Before
    public void setup() {
        try {
            this.dao = new YamlServiceRegistryDao(RESOURCE, false);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
