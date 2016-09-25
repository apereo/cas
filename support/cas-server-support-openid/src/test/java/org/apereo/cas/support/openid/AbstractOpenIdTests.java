package org.apereo.cas.support.openid;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.config.OpenIdConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.runner.RunWith;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Bootstrap context for openid tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {OpenIdConfiguration.class,
                CasProtocolViewsConfiguration.class,
                CasCookieConfiguration.class,
                CasValidationConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCoreConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasCoreUtilConfiguration.class})
@ContextConfiguration(locations = "classpath:/openid-config.xml")
public class AbstractOpenIdTests {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("serverManager")
    protected ServerManager serverManager;

    @Autowired
    @Qualifier("openIdServiceFactory")
    protected OpenIdServiceFactory openIdServiceFactory;

    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    public OpenIdServiceFactory getOpenIdServiceFactory() {
        return openIdServiceFactory;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return authenticationSystemSupport;
    }

}


