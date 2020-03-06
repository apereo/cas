package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasGoogleAnalyticsTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CreateGoogleAnalyticsCookieActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseCasGoogleAnalyticsTests.SharedTestConfiguration.class,
    properties = {
        "cas.google-analytics.cookie.attribute-name=membership",
        "cas.google-analytics.cookie.attribute-value-pattern=^(faculty|staff).*"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Webflow")
public class CreateGoogleAnalyticsCookieActionTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("createGoogleAnalyticsCookieAction")
    private Action createGoogleAnalyticsCookieAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val attributes = CollectionUtils.<String, List<Object>>wrap("membership", List.of("faculty", "staff", "student"));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser", attributes), context);
        val event = createGoogleAnalyticsCookieAction.execute(context);
        assertNull(event);
        val cookie = response.getCookie(casProperties.getGoogleAnalytics().getCookie().getName());
        assertNotNull(cookie);
        assertEquals("faculty,staff", cookie.getValue());
    }
}
