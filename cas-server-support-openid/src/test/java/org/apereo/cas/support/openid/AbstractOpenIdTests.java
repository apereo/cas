package org.apereo.cas.support.openid;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.config.OpenIdConfiguration;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.junit.runner.RunWith;
import org.openid4java.server.ServerAssociationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Bootstrap context for openid tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations = "classpath:/openid-config.xml",
        classes = {OpenIdConfiguration.class, CasProtocolViewsConfiguration.class})
public class AbstractOpenIdTests {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("openIdServiceFactory")
    protected OpenIdServiceFactory openIdServiceFactory;

    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("serverAssociations")
    protected ServerAssociationStore sharedAssociations;
    
    public OpenIdServiceFactory getOpenIdServiceFactory() {
        return openIdServiceFactory;
    }

    public CentralAuthenticationService getCentralAuthenticationService() {
        return centralAuthenticationService;
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return authenticationSystemSupport;
    }

    public ServerAssociationStore getSharedAssociations() {
        return sharedAssociations;
    }
}


