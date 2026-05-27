package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.config.CasValidationAutoConfiguration;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link ProxyValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasThymeleafAutoConfiguration.class,
    CasValidationAutoConfiguration.class
}, properties = "cas.ticket.st.time-to-kill-in-seconds=120")
@Tag("CAS")
@ExtendWith(CasTestExtension.class)
@Getter
@AutoConfigureMockMvc
class ProxyValidateControllerTests {
    protected static final String SUCCESS = "Success";

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @BeforeEach
    void before() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyValidServiceTicket() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("https://www.casinthecloud.com");
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);

        val result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY_VALIDATE)
                .param(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
                .param(CasProtocolConstants.PARAMETER_TICKET, sId.getId()))
            .andExpect(status().isOk())
            .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(SUCCESS));
    }

    @Test
    void verifyProxyTicket() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val service = RegisteredServiceTestUtils.getService("http://localhost:%s".formatted(webServer.getPort()));
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            registeredService.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy().setUseServiceId(true));
            servicesManager.save(registeredService);

            val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), service);

            val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
            val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), service, ctx);

            var result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY_VALIDATE)
                    .param(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
                    .param(CasProtocolConstants.PARAMETER_TICKET, sId.getId())
                    .param(CasProtocolConstants.PARAMETER_PROXY_CALLBACK_URL, service.getId()))
                .andExpect(status().isOk())
                .andReturn();
            assertTrue(result.getResponse().getContentAsString().contains(SUCCESS));
            var mv = result.getModelAndView();
            assertNotNull(mv);
            val pgt = mv.getModelMap().get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET).toString();

            result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY)
                    .param(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service.getId())
                    .param(CasProtocolConstants.PARAMETER_PROXY_GRANTING_TICKET, pgt))
                .andExpect(status().isOk())
                .andReturn();
            mv = result.getModelAndView();
            assertNotNull(mv);
            val pt = mv.getModelMap().get(CasProtocolConstants.PARAMETER_TICKET).toString();

            result = mockMvc.perform(get(CasProtocolConstants.ENDPOINT_PROXY_VALIDATE)
                    .param(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
                    .param(CasProtocolConstants.PARAMETER_TICKET, pt))
                .andExpect(status().isOk())
                .andReturn();
            assertTrue(result.getResponse().getContentAsString().contains(SUCCESS));
            assertTrue(result.getResponse().getContentAsString().contains("<cas:proxy>%s</cas:proxy>".formatted(service.getId())));
        }

    }
}
