package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.SwivelAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.SwivelConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SwivelAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    SwivelConfiguration.class,
    SwivelAuthenticationEventExecutionPlanConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.mfa.swivel.swivelUrl=http://localhost:9191",
    "cas.authn.mfa.swivel.sharedSecret=$ecret",
    "cas.authn.mfa.swivel.ignoreSslErrors=true"
})
public class SwivelAuthenticationHandlerTests {
    @Autowired
    @Qualifier("swivelAuthenticationHandler")
    private AuthenticationHandler swivelAuthenticationHandler;

    private MockWebServer webServer;

    @BeforeEach
    public void initialize() {
        val data = "<?xml version=\"1.0\" ?>"
            + "<SASResponse secret=\"MyAdminAgent\" version=\"3.4\">"
            + "<Version>3.6</Version>\n"
            + "<Result>PASS</Result>\n"
            + "<SessionID>c7379ef1b41f90a4900548a75e13f62a</SessionID>"
            + "</SASResponse>";
        this.webServer = new MockWebServer(9191,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE);
        this.webServer.start();
    }

    @AfterEach
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifySupports() {
        assertFalse(swivelAuthenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(swivelAuthenticationHandler.supports(new SwivelTokenCredential("123456")));
    }

    @Test
    public void verifyAuthn() throws Exception {
        val c = new SwivelTokenCredential("123456");
        val context = new MockRequestContext();
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        RequestContextHolder.setRequestContext(context);
        assertNotNull(swivelAuthenticationHandler.authenticate(c));
    }
}
