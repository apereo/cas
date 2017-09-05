package org.apereo.cas.support.openid.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.junit.Before;
import org.junit.Test;
import org.openid4java.association.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdServiceTests extends AbstractOpenIdTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdServiceTests.class);
    
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "openIdService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String OPEN_ID_PREFIX_URL = "http://openid.ja-sig.org/battags";
    private static final String RETURN_TO_URL = "http://www.ja-sig.org/?service=fa";

    private OpenIdService openIdService;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private Association association;

    @Before
    public void setUp() throws Exception {
        request.addParameter(OpenIdProtocolConstants.OPENID_IDENTITY, OPEN_ID_PREFIX_URL);
        request.addParameter(OpenIdProtocolConstants.OPENID_RETURNTO, RETURN_TO_URL);
        request.addParameter(OpenIdProtocolConstants.OPENID_MODE, "checkid_setup");
        association = this.serverManager.getSharedAssociations().generate(Association.TYPE_HMAC_SHA1, 2);
    }

    @Test
    public void verifySerializeAOpenIdServiceToJson() throws IOException {
        request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
        request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

        openIdService = openIdServiceFactory.createService(request);
        MAPPER.writeValue(JSON_FILE, openIdService);
        final OpenIdService serviceRead = MAPPER.readValue(JSON_FILE, OpenIdService.class);
        assertEquals(openIdService, serviceRead);
    }

    @Test
    public void verifyGetResponse() {
        try {
            request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
            request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

            openIdService = openIdServiceFactory.createService(request);
            final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), openIdService);

            final String tgt = centralAuthenticationService.createTicketGrantingTicket(ctx).getId();
            final String st = centralAuthenticationService.grantServiceTicket(tgt, openIdService, ctx).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            final Response response = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager, centralAuthenticationService)
                    .build(openIdService, "something", CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(response);

            assertEquals(association.getHandle(), response.getAttributes().get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE));
            assertEquals(RETURN_TO_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_RETURNTO));
            assertEquals(OPEN_ID_PREFIX_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_IDENTITY));

            final Response response2 = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager, centralAuthenticationService)
                    .build(openIdService, null, CoreAuthenticationTestUtils.getAuthentication());
            assertEquals("cancel", response2.getAttributes().get(OpenIdProtocolConstants.OPENID_MODE));
        } catch (final Exception e) {
            LOGGER.debug("Exception during verification of service ticket", e);
        }
    }

    @Test
    public void verifyExpiredAssociationGetResponse() {
        try {
            request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
            request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

            openIdService = openIdServiceFactory.createService(request);
            final AuthenticationResult ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), openIdService);
            final String tgt = centralAuthenticationService.createTicketGrantingTicket(ctx).getId();
            final String st = centralAuthenticationService.grantServiceTicket(tgt, openIdService, ctx).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (final InterruptedException e) {
                    fail("Could not wait long enough to check association expiry date");
                }
            }
            final Response response = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager, centralAuthenticationService)
                    .build(openIdService, st, CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(response);

            assertEquals(2, response.getAttributes().size());
            assertEquals("cancel", response.getAttributes().get(OpenIdProtocolConstants.OPENID_MODE));
        } catch (final Exception e) {
            LOGGER.debug("Exception during verification of service ticket", e);
        }
    }

    @Test
    public void verifyEquals() {
        final MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.addParameter("openid.identity", OPEN_ID_PREFIX_URL);
        request1.addParameter("openid.return_to", RETURN_TO_URL);
        request1.addParameter("openid.mode", "openid.checkid_setup");

        final MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addParameter("openid.identity", OPEN_ID_PREFIX_URL);
        request2.addParameter("openid.return_to", RETURN_TO_URL);

        final OpenIdService o1 = openIdServiceFactory.createService(request);
        final OpenIdService o2 = openIdServiceFactory.createService(request);

        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(new Object()));
    }
}
