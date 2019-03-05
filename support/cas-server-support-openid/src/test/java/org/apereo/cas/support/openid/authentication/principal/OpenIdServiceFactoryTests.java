package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link OpenIdServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        val factory = new OpenIdServiceFactory("");
        val service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationMissingReturn() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        val factory = new OpenIdServiceFactory("");
        val service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationMissingId() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        val factory = new OpenIdServiceFactory("");
        val service = factory.createService(request);
        assertNull(service);
    }
}
