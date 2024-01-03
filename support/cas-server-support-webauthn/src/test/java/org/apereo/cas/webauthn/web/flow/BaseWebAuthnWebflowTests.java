package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasSupportActionsAutoConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.config.WebAuthnComponentSerializationConfiguration;
import org.apereo.cas.config.WebAuthnConfiguration;
import org.apereo.cas.config.WebAuthnMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.WebAuthnWebflowConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseWebAuthnWebflowTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public abstract class BaseWebAuthnWebflowTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
        CasSupportActionsAutoConfiguration.class,
        WebAuthnComponentSerializationConfiguration.class,
        WebAuthnMultifactorProviderBypassConfiguration.class,
        WebAuthnConfiguration.class,
        WebAuthnWebflowConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
