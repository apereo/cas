package org.apereo.cas.oidc.web.flow;

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
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasOAuthAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * This is {@link OidcRegisteredServiceUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCookieConfiguration.class,
        CasThemesConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasOAuthConfiguration.class,
        CasOAuthAuthenticationServiceSelectionStrategyConfiguration.class,
        OidcConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class})
public class OidcRegisteredServiceUIActionTests {
    @Autowired
    @Qualifier("oidcRegisteredServiceUIAction")
    private Action oidcRegisteredServiceUIAction;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Test
    public void verifyOidcActionWithoutMDUI() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        WebUtils.putService(ctx, RegisteredServiceTestUtils.getService());
        final Event event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals(event.getId(), "success");
        assertNull(WebUtils.getServiceUserInterfaceMetadata(ctx, Serializable.class));
    }

    @Test
    public void verifyOidcActionWithMDUI() throws Exception {
        final OidcRegisteredService svc = new OidcRegisteredService();
        svc.setClientId("id");
        svc.setName("oauth");
        svc.setDescription("description");
        svc.setClientSecret("secret");
        svc.setInformationUrl("info");
        svc.setPrivacyUrl("privacy");
        svc.setServiceId("https://oauth\\.example\\.org.*");
        svc.setLogo("logo");
        servicesManager.save(svc);

        final MockRequestContext ctx = new MockRequestContext();
        WebUtils.putService(ctx, RegisteredServiceTestUtils.getService(
                "https://www.example.org?client_id=id&client_secret=secret&redirect_uri=https://oauth.example.org"));
        final Event event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals(event.getId(), "success");
        final DefaultRegisteredServiceUserInterfaceInfo mdui = WebUtils.getServiceUserInterfaceMetadata(ctx, DefaultRegisteredServiceUserInterfaceInfo.class);
        assertNotNull(mdui);

        assertEquals(mdui.getDisplayName(), svc.getName());
        assertEquals(mdui.getInformationURL(), svc.getInformationUrl());
        assertEquals(mdui.getDescription(), svc.getDescription());
        assertEquals(mdui.getPrivacyStatementURL(), svc.getPrivacyUrl());
        assertEquals(mdui.getLogoUrl(), svc.getLogo());
    }
}
