package org.apereo.cas.oidc.slo;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcSingleLogoutServiceLogoutUrlBuilderTests extends AbstractOidcTests {

    @BeforeEach
    public void initialize() {
        servicesManager.deleteAll();
        super.initialize();
    }

    @Test
    public void verifyOperation() {
        val id = UUID.randomUUID().toString();
        servicesManager.save(getOidcRegisteredService(id));

        assertTrue(singleLogoutServiceLogoutUrlBuilder.getOrder() > 0);
        val request = new MockHttpServletRequest();
        val service = RegisteredServiceTestUtils.getService("https://somewhere.org");
        assertFalse(singleLogoutServiceLogoutUrlBuilder.isServiceAuthorized(service, Optional.of(request)));

        request.addParameter(OAuth20Constants.CLIENT_ID, id);
        assertTrue(singleLogoutServiceLogoutUrlBuilder.isServiceAuthorized(
            RegisteredServiceTestUtils.getService(), Optional.of(request)));
    }

}
