package org.apereo.cas.pm;

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
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPasswordValidationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    PasswordManagementConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true",
    "cas.authn.pm.core.password-policy-pattern=^Th!.+{8,10}"
})
@Tag("PasswordOps")
class DefaultPasswordValidationServiceTests {
    @Autowired
    @Qualifier(PasswordValidationService.BEAN_NAME)
    private PasswordValidationService passwordValidationService;

    @Autowired
    @Qualifier(PasswordHistoryService.BEAN_NAME)
    private PasswordHistoryService passwordHistoryService;

    @Test
    void verifyReuseOldPassword() throws Throwable {
        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "123456".toCharArray(), "123456".toCharArray());
        assertFalse(passwordValidationService.isValid(request));
        request.setPassword("This!$P@$$".toCharArray());
        request.setConfirmedPassword("This!$P@$$".toCharArray());
        assertFalse(passwordValidationService.isValid(request));
    }

    @Test
    void verifyValidity() throws Throwable {
        assertFalse(passwordValidationService.isValid(
            new PasswordChangeRequest("casuser", "current-psw".toCharArray(), null, null)));
        assertFalse(passwordValidationService.isValid(
            new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "password".toCharArray(), "password".toCharArray())));
        assertFalse(passwordValidationService.isValid(
            new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "Th!sIsT3st".toCharArray(), "password".toCharArray())));

        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "Th!sIsT3st".toCharArray(), "Th!sIsT3st".toCharArray());
        assertTrue(passwordValidationService.isValid(request));

        passwordHistoryService.store(request);
        assertFalse(passwordValidationService.isValid(request));
    }
}
