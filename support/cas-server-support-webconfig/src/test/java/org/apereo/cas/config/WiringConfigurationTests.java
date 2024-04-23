package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
    CasWebAppAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreLoggingAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreValidationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,

    WebMvcAutoConfiguration.class,
    AopAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
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
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableWebMvc
@Tag("WebApp")
class WiringConfigurationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("rootController")
    private Controller rootController;

    @Autowired
    @Qualifier("localeResolver")
    private LocaleResolver localeResolver;
    
    @Test
    void verifyConfigurationClasses() throws Throwable {
        assertNotNull(applicationContext);
        assertTrue(applicationContext.getBeanDefinitionCount() > 0);
    }

    @Test
    void verifyRootController() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpGet.METHOD_NAME);
        request.setRequestURI("/cas/example");
        request.setQueryString("param=value");
        assertNotNull(rootController.handleRequest(request, new MockHttpServletResponse()));
    }

    @Test
    void verifyLocale() throws Throwable {
        var request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.ENGLISH));
        assertEquals(Locale.ENGLISH, localeResolver.resolveLocale(request));

        request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.ITALIAN));
        assertEquals(Locale.ITALIAN, localeResolver.resolveLocale(request));
    }
    
}
