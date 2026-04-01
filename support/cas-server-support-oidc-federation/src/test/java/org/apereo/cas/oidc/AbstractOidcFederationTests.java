package org.apereo.cas.oidc;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasOAuth20AutoConfiguration;
import org.apereo.cas.config.CasOidcAutoConfiguration;
import org.apereo.cas.config.CasOidcFederationAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * This is {@link AbstractOidcFederationTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(
    classes = AbstractOidcFederationTests.SharedTestConfiguration.class,
    properties = {
        "spring.threads.virtual.enabled=true",
        "cas.audit.slf4j.use-single-line=true",

        "cas.server.name=https://sso.example.org/",
        "cas.server.prefix=https://sso.example.org/cas",

        "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class AbstractOidcFederationTests {

    @Autowired
    protected CasConfigurationProperties casProperties;

    protected static MockHttpServletRequest getHttpRequestForEndpoint(final String endpoint) {
        val request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("sso.example.org");
        request.setServerPort(443);
        request.setRequestURI("/cas/oidc/" + endpoint);
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        return request;
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @ImportAutoConfiguration({
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasThymeleafAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasOidcAutoConfiguration.class,
        CasOAuth20AutoConfiguration.class,
        CasOidcFederationAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
