package org.apereo.cas.support.x509.rest.config;

import module java.base;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreRestAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRestAutoConfiguration;
import org.apereo.cas.config.CasThrottlingAutoConfiguration;
import org.apereo.cas.config.CasX509CertificateExtractorAutoConfiguration;
import org.apereo.cas.config.CasX509RestAutoConfiguration;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509RestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasX509CertificateExtractorAutoConfiguration.class,
    CasX509RestAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasThrottlingAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreRestAutoConfiguration.class,
    CasRestAutoConfiguration.class
},
    properties = "cas.rest.x509.tls-client-auth=true")
@Tag("X509")
@ExtendWith(CasTestExtension.class)
class X509RestConfigurationTests {
    @Autowired
    @Qualifier("x509RestMultipartBody")
    private RestHttpRequestCredentialFactory x509RestMultipartBody;

    @Autowired
    @Qualifier("x509RestRequestHeader")
    private RestHttpRequestCredentialFactory x509RestRequestHeader;

    @Autowired
    @Qualifier("x509RestTlsClientCert")
    private RestHttpRequestCredentialFactory x509RestTlsClientCert;
    
    @Test
    void verifyOperation() {
        assertNotNull(x509RestMultipartBody);
        assertNotNull(x509RestRequestHeader);
        assertNotNull(x509RestTlsClientCert);
    }
}
