package org.apereo.cas.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is {@link CoreSamlConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
        classes = {RefreshAutoConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreUtilConfiguration.class,
                CoreSamlConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCoreServicesConfiguration.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class CoreSamlConfigurationTests {

    @Test
    public void verify() {}
}
