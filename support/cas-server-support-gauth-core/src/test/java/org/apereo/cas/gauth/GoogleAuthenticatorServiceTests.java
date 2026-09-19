package org.apereo.cas.gauth;

import module java.base;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.DummyCredentialRepository;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("MFAProvider")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
}, properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class GoogleAuthenticatorServiceTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() {
        ClientInfoHolder.setClientInfo(ClientInfo.from(clientRequest("/login")));
        val googleAuth = new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor);
        googleAuth.setCredentialRepository(new DummyCredentialRepository());
        assertNotNull(googleAuth.getCredentialRepository());
        val key = googleAuth.createCredentials("casuser");
        assertNotNull(key);
        assertFalse(googleAuth.authorize(key.getKey(), key.getVerificationCode()));
    }

    @Test
    void verifyOperationForTenant() {
        ClientInfoHolder.setClientInfo(ClientInfo.from(clientRequest("/tenants/shire/login")));
        val googleAuth = new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor);
        googleAuth.setCredentialRepository(new DummyCredentialRepository());
        assertNotNull(googleAuth.getCredentialRepository());
        val key = googleAuth.createCredentials("casuser");
        assertNotNull(key);
        assertFalse(googleAuth.authorize(key.getKey(), key.getVerificationCode()));
    }

    /**
     * Both tests set the client info explicitly so neither inherits whatever the other left on a
     * pooled worker thread; the tenant is resolved from the context path.
     *
     * @param contextPath the context path
     * @return the request
     */
    private static MockHttpServletRequest clientRequest(final String contextPath) {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath(contextPath);
        return request;
    }
}
