package org.apereo.cas.oidc.federation;

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
import org.apereo.cas.config.CasOidcFederationAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link AbstractOidcTrustAnchorFederationTests}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@SpringBootTest(
    classes = AbstractOidcTrustAnchorFederationTests.SharedTestConfiguration.class,
    properties = {
        "cas.server.name=https://sso.example.org/",
        "cas.server.prefix=https://sso.example.org/cas",
        "cas.authn.oidc.federation.role=TRUST_ANCHOR",
        "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/federation.jwks",
        "cas.authn.oidc.core.issuer=https://sso.example.org/cas/oidc"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractOidcTrustAnchorFederationTests extends AbstractOidcFederationTests {

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
        CasOidcFederationAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
