package org.apereo.cas.config;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.AcceptUsersAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.Controller;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WiringConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasFiltersConfiguration.class,
    CasPropertiesConfiguration.class,
    CasWebAppConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    AcceptUsersAuthenticationEventExecutionPlanConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasLoggingConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreValidationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "cas.authn.accept.users=casuser::Mellon",
    "cas.http-web-request.cors.enabled=true",
    "cas.http-web-request.pattern-to-block=.*",
    "cas.http-web-request.header.content-security-policy=policy"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@WebAppConfiguration
@EnableAspectJAutoProxy
@EnableWebMvc
@Tag("WebApp")
public class WiringConfigurationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("rootController")
    private Controller rootController;

    @Autowired
    @Qualifier("localeResolver")
    private LocaleResolver localeResolver;

    @Autowired
    @Qualifier("currentCredentialsAndAuthenticationClearingFilter")
    private FilterRegistrationBean currentCredentialsAndAuthenticationClearingFilter;

    @Test
    public void verifyConfigurationClasses() {
        assertNotNull(applicationContext);
        assertTrue(applicationContext.getBeanDefinitionCount() > 0);
    }

    @Test
    public void verifyRootController() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpGet.METHOD_NAME);
        request.setRequestURI("/cas/example");
        request.setQueryString("param=value");
        assertNotNull(rootController.handleRequest(request, new MockHttpServletResponse()));
    }

    @Test
    public void verifyLocale() {
        var request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.ENGLISH));
        assertEquals(Locale.ENGLISH, localeResolver.resolveLocale(request));

        request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.ITALIAN));
        assertEquals(Locale.ITALIAN, localeResolver.resolveLocale(request));
    }

    @Test
    public void verifyAuthFilterClearing() throws Exception {
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(CoreAuthenticationTestUtils.getAuthentication());
        val filter = currentCredentialsAndAuthenticationClearingFilter.getFilter();
        filter.init(new MockFilterConfig());
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
        filter.destroy();
        assertNull(AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication());
        assertNull(AuthenticationCredentialsThreadLocalBinder.getCurrentAuthenticationBuilder());
        assertNull(AuthenticationCredentialsThreadLocalBinder.getCurrentCredentialIds());
        assertNull(AuthenticationCredentialsThreadLocalBinder.getCurrentCredentialIdsAsString());
        assertNull(AuthenticationCredentialsThreadLocalBinder.getInProgressAuthentication());
    }

}
