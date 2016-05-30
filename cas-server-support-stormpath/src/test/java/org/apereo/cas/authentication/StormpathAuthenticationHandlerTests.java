package org.apereo.cas.authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * This is {@link StormpathAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/stormpath-context.xml")
public class StormpathAuthenticationHandlerTests {
    @Resource(name="stormpathAuthenticationHandler")
    private StormpathAuthenticationHandler authenticationHandler;

    @Test
    public void verifyAuthentication() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword("casuser", "12345678mM"));
        assertEquals(result.getPrincipal().getId(), "casuser");
        assertTrue(result.getPrincipal().getAttributes().containsKey("fullName"));
        assertTrue(result.getPrincipal().getAttributes().containsKey("email"));
    }
}
