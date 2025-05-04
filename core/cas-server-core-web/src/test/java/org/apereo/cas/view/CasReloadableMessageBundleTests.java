package org.apereo.cas.view;

import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasReloadableMessageBundleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreWebAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
}, properties = {
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json"
})
class CasReloadableMessageBundleTests {

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Test
    void verifyMessage() {
        assertEquals("cas.message", messageSource.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, null, Locale.ENGLISH));
        assertEquals("cas.message", messageSource.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, null, Locale.ITALIAN));
        assertEquals("Hallo!", messageSource.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, "Hallo!", Locale.GERMAN));
    }

    @Test
    void verifyTenantMessage() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath("/tenants/shire/login");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        assertEquals("Hello, World!", messageSource.getMessage("cas.message",
            ArrayUtils.EMPTY_STRING_ARRAY, null, Locale.ENGLISH));
    }
}
