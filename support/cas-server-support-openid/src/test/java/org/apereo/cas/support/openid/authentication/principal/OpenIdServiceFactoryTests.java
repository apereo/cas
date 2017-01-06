package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Test cases for {@link OpenIdServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceFactoryTests {

    @Test
    public void verifyServiceCreationSuccessfullyById() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory("");
        final WebApplicationService service = factory.createService(request);
        assertNotNull(service);
    }

    @Test
    public void verifyServiceCreationMissingReturn() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "identity");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory("");
        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }

    @Test
    public void verifyServiceCreationMissingId() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "test");
        final OpenIdServiceFactory factory = new OpenIdServiceFactory("");
        final WebApplicationService service = factory.createService(request);
        assertNull(service);
    }
}
