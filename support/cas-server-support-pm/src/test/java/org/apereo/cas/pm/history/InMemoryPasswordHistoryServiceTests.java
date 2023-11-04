package org.apereo.cas.pm.history;

import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true"
})
@Tag("PasswordOps")
class InMemoryPasswordHistoryServiceTests {
    @Autowired
    @Qualifier(PasswordHistoryService.BEAN_NAME)
    private PasswordHistoryService passwordHistoryService;

    @Test
    void verifyValidity() throws Throwable {
        passwordHistoryService.removeAll();
        assertTrue(passwordHistoryService.fetchAll().isEmpty());

        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
        assertFalse(passwordHistoryService.exists(request));
        assertTrue(passwordHistoryService.store(request));
        assertTrue(passwordHistoryService.exists(request));
        assertFalse(passwordHistoryService.fetchAll().isEmpty());
        assertFalse(passwordHistoryService.fetch("casuser").isEmpty());

        passwordHistoryService.remove("casuser");
        assertTrue(passwordHistoryService.fetch("casuser").isEmpty());
    }
}
