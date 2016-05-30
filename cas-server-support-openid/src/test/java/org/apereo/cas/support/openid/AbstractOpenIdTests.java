package org.apereo.cas.support.openid;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.apereo.cas.CentralAuthenticationService;
import org.junit.runner.RunWith;
import org.openid4java.server.ServerAssociationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Bootstrap context for openid tests.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/openid-config.xml")
public class AbstractOpenIdTests {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name="openIdServiceFactory")
    protected OpenIdServiceFactory openIdServiceFactory;

    @Resource(name="centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    @Resource(name="defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport;

    @Resource(name="serverAssociations")
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


