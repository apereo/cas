package org.apereo.cas.adaptors.x509;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGroovyConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJsonConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
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
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasPersonDirectoryJsonConfiguration.class,
        CasPersonDirectoryGroovyConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
