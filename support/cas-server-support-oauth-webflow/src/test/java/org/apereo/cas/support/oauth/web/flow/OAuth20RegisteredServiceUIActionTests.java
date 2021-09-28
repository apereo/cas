package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RegisteredServiceUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = BaseOAuth20WebflowTests.SharedTestConfiguration.class,
    properties = "spring.main.allow-bean-definition-overriding=true")
@Tag("OAuth")
public class OAuth20RegisteredServiceUIActionTests {
    @Autowired
    @Qualifier("oauth20RegisteredServiceUIAction")
    private Action oauth20RegisteredServiceUIAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    public void verifyOAuthActionWithoutMDUI() throws Exception {
        val ctx = new MockRequestContext();
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(ctx, service);
        val svc = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        servicesManager.save(svc);

        val event = oauth20RegisteredServiceUIAction.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        val mdui = WebUtils.getServiceUserInterfaceMetadata(ctx, Serializable.class);
        assertNull(mdui);
    }

    @Test
    public void verifyOAuthActionWithMDUI() throws Exception {
        val svc = new OAuthRegisteredService();
        svc.setClientId("id");
        svc.setName("oauth");
        svc.setDescription("description");
        svc.setClientSecret("secret");
        svc.setInformationUrl("info");
        svc.setPrivacyUrl("privacy");
        svc.setServiceId("https://oauth\\.example\\.org.*");
        svc.setLogo("logo");
        servicesManager.save(svc);

        val ctx = new MockRequestContext();
        val service = RegisteredServiceTestUtils.getService("https://www.example.org?client_id=id&client_secret=secret&redirect_uri=https://oauth.example.org");
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of("id"));
        
        WebUtils.putServiceIntoFlowScope(ctx, service);
        val event = oauth20RegisteredServiceUIAction.execute(ctx);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        val mdui = WebUtils.getServiceUserInterfaceMetadata(ctx, DefaultRegisteredServiceUserInterfaceInfo.class);
        assertNotNull(mdui);

        assertEquals(mdui.getDisplayName(), svc.getName());
        assertEquals(mdui.getInformationURL(), svc.getInformationUrl());
        assertEquals(mdui.getDescription(), svc.getDescription());
        assertEquals(mdui.getPrivacyStatementURL(), svc.getPrivacyUrl());
        assertEquals(mdui.getLogoUrl(), svc.getLogo());
    }
}
