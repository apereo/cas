package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openid4java.association.Association;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 * @deprecated 6.2
 */
@Slf4j
@Deprecated(since = "6.2.0")
@Tag("Simple")
public class OpenIdServiceTests extends AbstractOpenIdTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "openIdService.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String OPEN_ID_PREFIX_URL = "http://openid.ja-sig.org/battags";

    private static final String RETURN_TO_URL = "http://www.ja-sig.org/?service=fa";

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serverManager")
    private ServerManager serverManager;

    @Autowired
    @Qualifier("openIdServiceFactory")
    private OpenIdServiceFactory openIdServiceFactory;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

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
            val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport, openIdService);

            val tgt = centralAuthenticationService.createTicketGrantingTicket(ctx).getId();
            val st = centralAuthenticationService.grantServiceTicket(tgt, openIdService, ctx).getId();
            centralAuthenticationService.validateServiceTicket(st, openIdService);

            val response = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL,
                serverManager, centralAuthenticationService,
                getServicesManager())
                .build(openIdService, "something", CoreAuthenticationTestUtils.getAuthentication());
            assertNotNull(response);

            assertEquals(association.getHandle(), response.getAttributes().get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE));
            assertEquals(RETURN_TO_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_RETURNTO));
            assertEquals(OPEN_ID_PREFIX_URL, response.getAttributes().get(OpenIdProtocolConstants.OPENID_IDENTITY));

            val response2 = new OpenIdServiceResponseBuilder(OPEN_ID_PREFIX_URL, serverManager,
                centralAuthenticationService,
                getServicesManager())
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
            val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport, openIdService);
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
                getServicesManager())
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

        assertEquals(o2, o1);
        assertNotEquals(new Object(), o1);
    }

    private ServicesManager getServicesManager() {
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(mock(ServiceRegistry.class))
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        return new DefaultServicesManager(context);
    }
}
