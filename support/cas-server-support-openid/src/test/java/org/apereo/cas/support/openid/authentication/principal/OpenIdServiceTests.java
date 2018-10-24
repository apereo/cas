package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openid4java.association.Association;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
public class OpenIdServiceTests extends AbstractOpenIdTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "openIdService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String OPEN_ID_PREFIX_URL = "http://openid.ja-sig.org/battags";
    private static final String RETURN_TO_URL = "http://www.ja-sig.org/?service=fa";
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private OpenIdService openIdService;
    private Association association;

    @BeforeEach
    public void initialize() throws Exception {
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
        val serviceRead = MAPPER.readValue(JSON_FILE, OpenIdService.class);
        assertEquals(openIdService, serviceRead);
    }

    @Test
    public void verifyGetResponse() {
        try {
            request.removeParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
            request.addParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, association.getHandle());

            openIdService = openIdServiceFactory.createService(request);
            val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), openIdService);

            val tgt = centralAuthenticationService.createTicketGrantingTicket(ctx).getId();
            val st = centralAuthenticationService.grantServiceTicket(tgt, openIdService, ctx).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            val response = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL,
                serverManager, centralAuthenticationService,
                new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class), new HashSet<>()))
                .build(openIdService, "something", CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(response);

            assertEquals(association.getHandle(), response.getAttributes().get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE));
            assertEquals(RETURN_TO_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_RETURNTO));
            assertEquals(OPEN_ID_PREFIX_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_IDENTITY));

            val response2 = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager,
                centralAuthenticationService,
                new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class), new HashSet<>()))
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
            val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), openIdService);
            val tgt = centralAuthenticationService.createTicketGrantingTicket(ctx).getId();
            val st = centralAuthenticationService.grantServiceTicket(tgt, openIdService, ctx).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (final InterruptedException e) {
                    throw new AssertionError("Could not wait long enough to check association expiry date");
                }
            }
            val response = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager,
                centralAuthenticationService,
                new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class), new HashSet<>()))
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
        val request1 = new MockHttpServletRequest();
        request1.addParameter("openid.identity", OPEN_ID_PREFIX_URL);
        request1.addParameter("openid.return_to", RETURN_TO_URL);
        request1.addParameter("openid.mode", "openid.checkid_setup");

        val request2 = new MockHttpServletRequest();
        request2.addParameter("openid.identity", OPEN_ID_PREFIX_URL);
        request2.addParameter("openid.return_to", RETURN_TO_URL);

        val o1 = openIdServiceFactory.createService(request);
        val o2 = openIdServiceFactory.createService(request);

        assertTrue(o1.equals(o2));
        assertFalse(o1.equals(new Object()));
    }
}
