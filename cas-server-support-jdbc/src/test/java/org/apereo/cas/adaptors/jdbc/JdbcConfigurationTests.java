package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.adaptors.jdbc.config.CasJdbcConfiguration;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spockframework.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        classes = {RefreshAutoConfiguration.class, CasJdbcConfiguration.class, CasCoreServicesConfiguration.class}
        , initializers = ConfigFileApplicationContextInitializer.class)
public class JdbcConfigurationTests {
    
    @Autowired
    @Qualifier("searchModeSearchDatabaseAuthenticationHandler")
    private AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler;

    @Autowired
    @Qualifier("queryDatabaseAuthenticationHandler")
    private AuthenticationHandler queryDatabaseAuthenticationHandler;

    @Autowired
    @Qualifier("queryAndEncodeDatabaseAuthenticationHandler")
    private AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler;

    @Autowired
    @Qualifier("bindModeSearchDatabaseAuthenticationHandler")
    private AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler;
    
    @Test
    public void verifyConfiguration() {
        Assert.notNull(this.searchModeSearchDatabaseAuthenticationHandler);
        Assert.notNull(this.queryDatabaseAuthenticationHandler);
        Assert.notNull(this.queryAndEncodeDatabaseAuthenticationHandler);
        Assert.notNull(this.bindModeSearchDatabaseAuthenticationHandler);
    }
}
