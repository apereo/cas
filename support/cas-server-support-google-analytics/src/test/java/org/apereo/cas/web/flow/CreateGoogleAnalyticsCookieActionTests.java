package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasGoogleAnalyticsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    CasGoogleAnalyticsConfiguration.class,
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCookieConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.google-analytics.cookie.attribute-name=membership",
    "cas.google-analytics.cookie.attribute-value-pattern=^(faculty|staff).*",
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "spring.mail.testConnection=false"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
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
