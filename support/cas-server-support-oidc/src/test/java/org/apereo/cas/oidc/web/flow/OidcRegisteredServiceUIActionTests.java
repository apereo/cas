package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.Serializable;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcRegisteredServiceUIActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("OIDCWeb")
class OidcRegisteredServiceUIActionTests extends AbstractOidcTests {
    @Test
    void verifyOidcActionWithoutMDUI() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlowScope(context, null);
        val event = oidcRegisteredServiceUIAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertNull(WebUtils.getServiceUserInterfaceMetadata(context, Serializable.class));
    }

    @Test
    void verifyOidcActionWithMDUI() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val service = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(
            "https://www.example.org?client_id=%s&client_secret=%s&redirect_uri=https://oauth.example.org"
                .formatted(service.getClientId(), service.getClientSecret())));
        val event = oidcRegisteredServiceUIAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        val mdui = WebUtils.getServiceUserInterfaceMetadata(context, DefaultRegisteredServiceUserInterfaceInfo.class);
        assertNotNull(mdui);
        assertEquals(mdui.getDisplayName(), service.getName());
        assertEquals(mdui.getInformationURL(), service.getInformationUrl());
        assertEquals(mdui.getDescription(), service.getDescription());
        assertEquals(mdui.getPrivacyStatementURL(), service.getPrivacyUrl());
        assertEquals(mdui.getLogoUrl(), service.getLogo());
    }
}
