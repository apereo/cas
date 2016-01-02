package org.jasig.cas.authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoAuthenticationHandler}.
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/mongo-context.xml")
public class MongoAuthenticationHandlerTests {

    @Autowired
    @Qualifier("mongoAuthenticationHandler")
    private MongoAuthenticationHandler authenticationHandler;

    @Test
    public void verifyAuthentication() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword("u1", "p1"));
        assertEquals(result.getPrincipal().getId(), "u1");
        assertTrue(result.getPrincipal().getAttributes().containsKey("loc"));
        assertTrue(result.getPrincipal().getAttributes().containsKey("state"));
    }

}
