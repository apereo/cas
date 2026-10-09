package org.apereo.cas.support.saml.web.idp.profile.slo;

import module java.base;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.HttpMessage;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPSingleLogoutServiceMessageHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLLogout")
class SamlIdPSingleLogoutServiceMessageHandlerTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlSingleLogoutServiceMessageHandler")
    private SingleLogoutServiceMessageHandler samlSingleLogoutServiceMessageHandler;

    @Autowired
    @Qualifier("samlIdPLogoutResponseObjectBuilder")
    private Saml20ObjectBuilder samlIdPLogoutResponseObjectBuilder;
    
    private SamlRegisteredService samlRegisteredService;

    @BeforeEach
    void beforeEach() {
        samlRegisteredService = getSamlRegisteredServiceForTestShib();
        servicesManager.save(samlRegisteredService);

        val service = new SamlRegisteredService();
        service.setName("Mocky");
        service.setServiceId("https://mocky.io");
        service.setId(RandomUtils.nextInt());
        service.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        servicesManager.save(service);

        val registeredService = new SamlRegisteredService();
        registeredService.setName("MockySoap");
        registeredService.setServiceId("urn:soap:slo:example");
        registeredService.setId(RandomUtils.nextInt());
        registeredService.setMetadataLocation("classpath:metadata/testshib-providers.xml");
        servicesManager.save(registeredService);
    }

    @Test
    void verifySupports() {
        val service = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(samlRegisteredService.getServiceId()));
        val ctx = SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build();
        assertTrue(samlSingleLogoutServiceMessageHandler.supports(ctx, service));
        assertEquals(0, samlSingleLogoutServiceMessageHandler.getOrder());
    }

    @Test
    void verifySendByPost() {
        val service = RegisteredServiceTestUtils.getService(samlRegisteredService.getServiceId());
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(samlRegisteredService.getServiceId()));

        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    void verifyNoSaml() {
        val registeredService = getSamlRegisteredServiceForTestShib();
        servicesManager.save(registeredService);
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(registeredService.getServiceId()));
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    void verifySendByRedirect() {
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(samlRegisteredService.getServiceId()));
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    void verifySkipLogoutForOriginator() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("https://mocky.io");
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(samlRegisteredService.getServiceId()));
        
        val request = new MockHttpServletRequest();
        val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()),
            "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer(service.getId()),
            UUID.randomUUID().toString(),
            samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }
        val response = new MockHttpServletResponse();
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
            SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .httpServletRequest(Optional.of(request))
                .httpServletResponse(Optional.of(response))
                .build());
        assertFalse(result.isEmpty());
    }

    @Test
    void verifySoap() {
        val service = RegisteredServiceTestUtils.getService("urn:soap:slo:example");
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(service.getId()));
        val result = samlSingleLogoutServiceMessageHandler.handle(service, "ST-1234567890",
                SingleLogoutExecutionRequest.builder().ticketGrantingTicket(new MockTicketGrantingTicket("casuser")).build());
        assertFalse(result.isEmpty());
    }

    @Test
    void verifySoapMessagePreparation() {
        val properties = new HashMap<String, String>();
        properties.put(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING, SAMLConstants.SAML2_SOAP11_BINDING_URI);
        val logoutRequest = DefaultSingleLogoutRequestContext.builder().properties(properties).build();
        val logoutMsg = SingleLogoutMessage.builder().payload("SOAP_envelop").build();
        val msg = samlSingleLogoutServiceMessageHandler.prepareLogoutHttpMessageToSend(logoutRequest, logoutMsg);
        assertNotNull(msg);
        assertEquals(MediaType.TEXT_XML_VALUE, msg.getContentType());
        assertNull(((LogoutHttpMessage) msg).getLogoutRequestParameter());
    }

    @Test
    void verifyBackChannelLogoutDispatchedWhenAsynchronous() throws Throwable {
        val sendingThreads = new LinkedBlockingQueue<Thread>();
        val context = newLogoutRequestContext("https://sp.example.org", new MockHttpServletRequest());

        val asynchronousHandler = newMessageHandler(true, openSamlConfigBean, sendingThreads);
        try {
            assertTrue(asynchronousHandler.sendMessageToEndpoint(newLogoutHttpMessage(), context, newLogoutMessage()));
            val sendingThread = sendingThreads.poll(10, TimeUnit.SECONDS);
            assertNotNull(sendingThread);
            assertNotSame(Thread.currentThread(), sendingThread);
        } finally {
            asynchronousHandler.destroy();
        }

        val synchronousHandler = newMessageHandler(false, openSamlConfigBean, sendingThreads);
        try {
            assertFalse(synchronousHandler.sendMessageToEndpoint(newLogoutHttpMessage(), context, newLogoutMessage()));
            assertSame(Thread.currentThread(), sendingThreads.poll());
        } finally {
            synchronousHandler.destroy();
        }
    }

    @Test
    void verifyLogoutRequestParsedOncePerRequest() throws Throwable {
        val configBean = mock(OpenSamlConfigBean.class, delegatesTo(openSamlConfigBean));
        val handler = newMessageHandler(false, configBean, new LinkedBlockingQueue<>());
        try {
            val request = new MockHttpServletRequest();
            WebUtils.putSingleLogoutRequest(request, newEncodedLogoutRequest("https://initiator.example.org"));
            assertTrue(handler.sendMessageToEndpoint(newLogoutHttpMessage(),
                newLogoutRequestContext("https://initiator.example.org", request), newLogoutMessage()));
            assertFalse(handler.sendMessageToEndpoint(newLogoutHttpMessage(),
                newLogoutRequestContext("https://other.example.org", request), newLogoutMessage()));
            verify(configBean, times(1)).getParserPool();
        } finally {
            handler.destroy();
        }
    }

    private SamlIdPSingleLogoutServiceMessageHandler newMessageHandler(final boolean asynchronous,
                                                                      final OpenSamlConfigBean configBean,
                                                                      final Queue<Thread> sendingThreads) {
        val handler = (SamlIdPSingleLogoutServiceMessageHandler) samlSingleLogoutServiceMessageHandler;
        return new SamlIdPSingleLogoutServiceMessageHandler(handler.getHttpClient(), handler.getLogoutMessageBuilder(),
            handler.getServicesManager(), handler.getSingleLogoutServiceLogoutUrlBuilder(), asynchronous,
            handler.getAuthenticationRequestServiceSelectionStrategies(), handler.getSamlRegisteredServiceCachingMetadataResolver(),
            handler.getVelocityEngineFactory(), configBean) {
            @Override
            protected boolean sendLogoutRequest(final HttpMessage msg, final SingleLogoutMessage logoutMessage, final String binding) {
                sendingThreads.add(Thread.currentThread());
                return false;
            }
        };
    }

    private String newEncodedLogoutRequest(final String issuer) throws Throwable {
        val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()),
            "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer(issuer),
            UUID.randomUUID().toString(),
            samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
            return EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static SingleLogoutRequestContext newLogoutRequestContext(final String serviceId, final MockHttpServletRequest request) {
        return DefaultSingleLogoutRequestContext.builder()
            .service(RegisteredServiceTestUtils.getService(serviceId))
            .properties(Map.of(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING, SAMLConstants.SAML2_POST_BINDING_URI))
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .httpServletRequest(Optional.of(request))
                .build())
            .build();
    }

    private static HttpMessage newLogoutHttpMessage() throws Exception {
        return new LogoutHttpMessage(new URI("https://sp.example.org/slo").toURL(), "payload", true);
    }

    private static SingleLogoutMessage newLogoutMessage() {
        return SingleLogoutMessage.builder().payload("payload").build();
    }
}
