package org.apereo.cas.oidc.slo;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
class OidcSingleLogoutServiceLogoutUrlBuilderTests extends AbstractOidcTests {

    @Override
    @BeforeEach
    protected void initialize() throws Throwable {
        servicesManager.deleteAll();
        super.initialize();
    }

    @Test
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        servicesManager.save(getOidcRegisteredService(id));

        assertTrue(singleLogoutServiceLogoutUrlBuilder.getOrder() > 0);
        val request = new MockHttpServletRequest();
        val service = RegisteredServiceTestUtils.getService("https://somewhere.org");
        assertFalse(singleLogoutServiceLogoutUrlBuilder.isServiceAuthorized(service, Optional.of(request), Optional.of(new MockHttpServletResponse())));
    }

}
