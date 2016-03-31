package org.jasig.cas.support.openid;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.junit.runner.RunWith;
import org.openid4java.server.ServerAssociationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Bootstrap context for openid tests.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/META-INF/spring/openid-config.xml"})
public class AbstractOpenIdTests {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

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


