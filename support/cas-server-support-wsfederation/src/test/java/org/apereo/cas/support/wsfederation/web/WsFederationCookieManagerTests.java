package org.apereo.cas.support.wsfederation.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationCookieManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WSFederation")
class WsFederationCookieManagerTests extends AbstractWsFederationTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.setRemoteAddr("185.86.151.11");
        context.setLocalAddr("185.88.151.11");
        context.withUserAgent();
        context.setClientInfo();

        context.setParameter(CasProtocolConstants.PARAMETER_METHOD, "POST");
        context.setRequestAttribute("locale", "en");
        context.setRequestAttribute("theme", "custom");

        val config = wsFederationConfigurations.toList().getFirst();
        val wctx = config.getId();
        val original = RegisteredServiceTestUtils.getService();
        wsFederationCookieManager.store(context.getHttpServletRequest(), context.getHttpServletResponse(), wctx, original, config);

        context.setParameter(WsFederationCookieManager.WCTX, wctx);
        context.setRequestCookiesFromResponse();
        val service = wsFederationCookieManager.retrieve(context);
        assertNotNull(service);
        assertEquals(original.getId(), service.getId());
    }

    @Test
    void verifyNoContext() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertThrows(IllegalArgumentException.class, () -> wsFederationCookieManager.retrieve(context));
    }

    @Test
    void verifyNoCookieValue() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val config = wsFederationConfigurations.toList().getFirst();
        val wctx = config.getId();
        context.setParameter(WsFederationCookieManager.WCTX, wctx);
        assertThrows(IllegalArgumentException.class, () -> wsFederationCookieManager.retrieve(context));
    }
}
