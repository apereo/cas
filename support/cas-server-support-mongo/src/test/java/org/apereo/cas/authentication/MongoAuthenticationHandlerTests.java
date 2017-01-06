package org.apereo.cas.authentication;

import org.apereo.cas.authentication.config.CasMongoAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CasMongoAuthenticationConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                RefreshAutoConfiguration.class})
@EnableScheduling
@TestPropertySource(locations={"classpath:/mongo.properties"})
public class MongoAuthenticationHandlerTests {

    @Autowired
    @Qualifier("mongoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Before
    public void setup() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }
    @Test
    public void verifyAuthentication() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(CoreAuthenticationTestUtils
                .getCredentialsWithDifferentUsernameAndPassword("u1", "p1"));
        assertEquals(result.getPrincipal().getId(), "u1");
        assertTrue(result.getPrincipal().getAttributes().containsKey("loc"));
        assertTrue(result.getPrincipal().getAttributes().containsKey("state"));
    }
}
