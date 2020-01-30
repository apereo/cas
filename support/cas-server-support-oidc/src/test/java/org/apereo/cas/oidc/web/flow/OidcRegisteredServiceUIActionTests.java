package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.authentication.principal.OAuthApplicationServiceFactory;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("OIDC")
public class OidcRegisteredServiceUIActionTests extends AbstractOidcTests {

    @Test
    public void verifyOidcActionWithoutMDUI() throws Exception {
        val ctx = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(ctx, null);
        val event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals("success", event.getId());
        assertNull(WebUtils.getServiceUserInterfaceMetadata(ctx, Serializable.class));
    }

    @Test
    public void verifyOidcActionWithMDUI() throws Exception {
        val ctx = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(ctx, getOauthService(
            "https://www.example.org?client_id=clientid&client_secret=secret&redirect_uri=https://oauth.example.org"));
        val event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals("success", event.getId());
        val mdui = WebUtils.getServiceUserInterfaceMetadata(ctx, DefaultRegisteredServiceUserInterfaceInfo.class);
        assertNotNull(mdui);

        val svc = getOidcRegisteredService();
        assertEquals(mdui.getDisplayName(), svc.getName());
        assertEquals(mdui.getInformationURL(), svc.getInformationUrl());
        assertEquals(mdui.getDescription(), svc.getDescription());
        assertEquals(mdui.getPrivacyStatementURL(), svc.getPrivacyUrl());
        assertEquals(mdui.getLogoUrl(), svc.getLogo());
    }

    public static AbstractWebApplicationService getOauthService(final String name) {
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, name);
        return (AbstractWebApplicationService) new OAuthApplicationServiceFactory().createService(request);
    }
}
