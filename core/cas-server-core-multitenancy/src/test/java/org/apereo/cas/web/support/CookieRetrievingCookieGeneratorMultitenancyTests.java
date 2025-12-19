package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.BaseMultitenancyTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorMultitenancyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Cookie")
@SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class,
    properties = {
        "cas.tgc.path=/cas",
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class CookieRetrievingCookieGeneratorMultitenancyTests {
    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Test
    void verifyTenantCookiePath() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.12");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpHeaders.USER_AGENT, "firefox");
        request.setContextPath("/cas/tenants/b9584c42/login");
        val response = new MockHttpServletResponse();
        val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, response, "TGT-123456");
        assertNotNull(cookie);
        assertEquals("/cas/tenants/b9584c42", cookie.getPath());
    }

}
