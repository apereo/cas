package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasGoogleAnalyticsTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasGoogleAnalyticsCookieGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasGoogleAnalyticsTests.SharedTestConfiguration.class,
    properties = {
        "cas.google-analytics.cookie.name=CasGoogleCookie",
        "cas.google-analytics.cookie.sameSitePolicy=strict",
        "cas.tgc.cookie.sameSitePolicy=lax"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasGoogleAnalyticsCookieGeneratorTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casGoogleAnalyticsCookieGenerator")
    private CasCookieBuilder casGoogleAnalyticsCookieGenerator;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @BeforeAll
    public static void setup() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("107.181.69.221");
        request.setLocalAddr("127.0.0.1");

        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }
    
    @Test
    public void verifyCookieValue() {
        val request = new MockHttpServletRequest();
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");

        val response = new MockHttpServletResponse();
        casGoogleAnalyticsCookieGenerator.addCookie(request, response, "value");
        ticketGrantingTicketCookieGenerator.addCookie(request, response, "value");
        assertNotNull(response.getCookie(casProperties.getGoogleAnalytics().getCookie().getName()));
        assertNotNull(response.getCookie(casProperties.getTgc().getName()));
    }
}
