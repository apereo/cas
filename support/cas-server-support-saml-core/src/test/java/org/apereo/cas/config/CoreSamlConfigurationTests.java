package org.apereo.cas.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link CoreSamlConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RefreshAutoConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreUtilConfiguration.class,
                CoreSamlConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCoreServicesConfiguration.class})
public class CoreSamlConfigurationTests {

    @Test
    public void verify() {}
}
