package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.adaptors.jdbc.config.CasJdbcConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This is {@link JdbcConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"},
        classes = {RefreshAutoConfiguration.class, CasCoreAuthenticationConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasJdbcConfiguration.class, CasCoreServicesConfiguration.class}, 
        initializers = ConfigFileApplicationContextInitializer.class)
public class JdbcConfigurationTests {
    
    @Test
    public void verifyConfiguration() {
        
    }
}
