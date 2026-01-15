package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.BaseCasGoogleAnalyticsTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
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
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
class CreateGoogleAnalyticsCookieActionTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_CREATE_COOKIE)
    private Action createGoogleAnalyticsCookieAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val attributes = CollectionUtils.<String, List<Object>>wrap("membership", List.of("faculty", "staff", "student"));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser", attributes), context);
        val event = createGoogleAnalyticsCookieAction.execute(context);
        assertNull(event);
        val cookie = context.getHttpServletResponse().getCookie(casProperties.getGoogleAnalytics().getCookie().getName());
        assertNotNull(cookie);
        assertEquals("faculty,staff", cookie.getValue());
    }
}
