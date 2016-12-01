package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.adaptors.jdbc.config.CasJdbcConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This is {@link JdbcConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, CasCoreAuthenticationConfiguration.class,
        CasCoreUtilConfiguration.class, CasPersonDirectoryConfiguration.class,
        CasJdbcConfiguration.class, CasCoreServicesConfiguration.class})
@ContextConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"})
@EnableScheduling
public class JdbcConfigurationTests {

    @Test
    public void verifyConfiguration() {
    }
}
