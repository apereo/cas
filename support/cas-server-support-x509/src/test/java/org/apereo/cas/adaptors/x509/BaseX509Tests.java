package org.apereo.cas.adaptors.x509;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGroovyConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJsonConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.X509AuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.X509AuthenticationConfiguration;
import org.apereo.cas.config.X509CertificateExtractorConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseX509Tests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseX509Tests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        X509AuthenticationConfiguration.class,
        X509CertificateExtractorConfiguration.class,
        X509AuthenticationComponentSerializationConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasPersonDirectoryJsonConfiguration.class,
        CasPersonDirectoryGroovyConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
