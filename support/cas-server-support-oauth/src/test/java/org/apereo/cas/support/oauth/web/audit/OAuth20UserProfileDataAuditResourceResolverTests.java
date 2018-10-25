package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20UserProfileDataAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20UserProfileDataAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        val r = new OAuth20UserProfileDataAuditResourceResolver();
        val token = mock(AccessToken.class);
        when(token.getId()).thenReturn("CODE");
        when(token.getService()).thenReturn(RegisteredServiceTestUtils.getService());

        val service = new OAuthRegisteredService();
        service.setClientId("CLIENTID");
        service.setName("OAUTH");
        service.setId(123);

        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{token});

        val result = r.resolveFrom(jp, CollectionUtils.wrap(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, "id",
            OAuth20Constants.CLIENT_ID, "clientid",
            CasProtocolConstants.PARAMETER_SERVICE, "service",
            "scopes", CollectionUtils.wrapSet("email"),
            "attributes", CollectionUtils.wrap("attributeName", "attributeValue")));
        assertTrue(result.length > 0);
    }
}
