package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link OpenIdServiceFactory}.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 4.2
 */
@Deprecated(since = "6.2.0")
@Tag("Simple")
public class OpenIdServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        val factory = new OpenIdServiceFactory(StringUtils.EMPTY);
        val service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationMissingReturn() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        val factory = new OpenIdServiceFactory(StringUtils.EMPTY);
        val service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationMissingId() {
        val request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        val factory = new OpenIdServiceFactory(StringUtils.EMPTY);
        val service = factory.createService(request);
        assertNull(service);
    }
}
