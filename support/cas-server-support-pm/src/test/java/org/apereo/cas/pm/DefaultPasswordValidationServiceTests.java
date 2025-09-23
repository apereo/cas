package org.apereo.cas.pm;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultPasswordValidationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasPasswordManagementAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true",
    "cas.authn.pm.core.password-policy-pattern=^Th!.+{8,10}"
})
@Tag("PasswordOps")
@ExtendWith(CasTestExtension.class)
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
