package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * This is {@link OidcRegisteredServiceUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcRegisteredServiceUIActionTests extends AbstractOidcTests {

    @Test
    public void verifyOidcActionWithoutMDUI() throws Exception {
        final var ctx = new MockRequestContext();
        WebUtils.putService(ctx, null);
        final var event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals("success", event.getId());
        assertNull(WebUtils.getServiceUserInterfaceMetadata(ctx, Serializable.class));
    }

    @Test
    public void verifyOidcActionWithMDUI() throws Exception {
        final var ctx = new MockRequestContext();
        WebUtils.putService(ctx, RegisteredServiceTestUtils.getService(
            "https://www.example.org?client_id=id&client_secret=secret&redirect_uri=https://oauth.example.org"));
        final var event = oidcRegisteredServiceUIAction.execute(ctx);
        assertEquals("success", event.getId());
        final var mdui = WebUtils.getServiceUserInterfaceMetadata(ctx, DefaultRegisteredServiceUserInterfaceInfo.class);
        assertNotNull(mdui);

        final var svc = getOidcRegisteredService();
        assertEquals(mdui.getDisplayName(), svc.getName());
        assertEquals(mdui.getInformationURL(), svc.getInformationUrl());
        assertEquals(mdui.getDescription(), svc.getDescription());
        assertEquals(mdui.getPrivacyStatementURL(), svc.getPrivacyUrl());
        assertEquals(mdui.getLogoUrl(), svc.getLogo());
    }
}
