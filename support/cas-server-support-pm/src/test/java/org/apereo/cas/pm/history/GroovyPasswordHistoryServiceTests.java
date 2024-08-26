package org.apereo.cas.pm.history;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    CasPasswordManagementAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
},
    properties = {
        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.history.core.enabled=true",
        "cas.authn.pm.history.groovy.location=classpath:PasswordHistoryService.groovy"
    })
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
class GroovyPasswordHistoryServiceTests {
    @Autowired
    @Qualifier(PasswordHistoryService.BEAN_NAME)
    private PasswordHistoryService passwordHistoryService;

    @Test
    void verifyValidity() throws Throwable {
        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
        assertFalse(passwordHistoryService.exists(request));
        assertTrue(passwordHistoryService.store(request));
        assertTrue(passwordHistoryService.fetchAll().isEmpty());
        assertTrue(passwordHistoryService.fetch("casuser").isEmpty());
        assertDoesNotThrow(() -> {
            passwordHistoryService.remove("casuser");
            passwordHistoryService.removeAll();
        });
    }
}
