package org.apereo.cas.gauth;

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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
@SpringBootTest(classes = CasCoreMultitenancyAutoConfiguration.class, properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class GoogleAuthenticatorServiceTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() {
        val googleAuth = new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor);
        googleAuth.setCredentialRepository(new DummyCredentialRepository());
        assertNotNull(googleAuth.getCredentialRepository());
        val key = googleAuth.createCredentials("casuser");
        assertNotNull(key);
        assertFalse(googleAuth.authorize(key.getKey(), key.getVerificationCode()));
    }

    @Test
    void verifyOperationForTenant() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath("/tenants/shire/login");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val googleAuth = new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor);
        googleAuth.setCredentialRepository(new DummyCredentialRepository());
        assertNotNull(googleAuth.getCredentialRepository());
        val key = googleAuth.createCredentials("casuser");
        assertNotNull(key);
        assertFalse(googleAuth.authorize(key.getKey(), key.getVerificationCode()));
    }
}
