package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.StormpathAuthenticationConfiguration;
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
 * This is {@link StormpathAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {StormpathAuthenticationConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                CasCoreServicesConfiguration.class,
                RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/stormpath.properties"})
@EnableScheduling
public class StormpathAuthenticationHandlerTests {
    @Autowired
    @Qualifier("stormpathAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Before
    public void setUp() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void verifyAuthentication() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(CoreAuthenticationTestUtils
                .getCredentialsWithDifferentUsernameAndPassword("casuser", "12345678mM"));
        assertEquals(result.getPrincipal().getId(), "casuser");
        assertTrue(result.getPrincipal().getAttributes().containsKey("fullName"));
        assertTrue(result.getPrincipal().getAttributes().containsKey("email"));
    }
}
