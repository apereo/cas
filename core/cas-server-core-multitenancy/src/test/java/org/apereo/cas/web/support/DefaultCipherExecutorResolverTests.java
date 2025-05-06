package org.apereo.cas.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.cookie.TicketGrantingCookieProperties;
import org.apereo.cas.multitenancy.BaseMultitenancyTests;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.DefaultCipherExecutorResolver;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCipherExecutorResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Cookie")
@SpringBootTest(classes = BaseMultitenancyTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class DefaultCipherExecutorResolverTests {

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;

    @Test
    void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.12");
        request.setLocalAddr("185.88.151.11");
        request.addHeader(HttpHeaders.USER_AGENT, "firefox");
        request.setContextPath("/cas/tenants/cookie/login");
        val resolver = new DefaultCipherExecutorResolver(CipherExecutor.noOp(), tenantExtractor,
            TicketGrantingCookieProperties.class, bindingContext -> {
            val properties = bindingContext.value();
            return CipherExecutorUtils.newStringCipherExecutor(properties.getTgc().getCrypto(), TicketGrantingCookieCipherExecutor.class);
        });
        val cipher = resolver.resolve(request);
        assertNotNull(cipher);
        assertTrue(cipher.isEnabled());
        val cookieValue = UUID.randomUUID().toString();
        val encoded = cipher.encode(cookieValue);
        assertNotEquals(cookieValue, encoded);
        val decoded = cipher.decode(encoded);
        assertEquals(cookieValue, decoded);
    }
}
