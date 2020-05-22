package org.apereo.cas.pm;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
    PasswordManagementConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.history.enabled=true",
    "cas.authn.pm.policyPattern=^Th!.+{8,10}"
})
@Tag("Simple")
public class DefaultPasswordValidationServiceTests {
    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Autowired
    @Qualifier("passwordHistoryService")
    private PasswordHistoryService passwordHistoryService;

    @Test
    public void verifyValidity() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password");
        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeRequest("user", StringUtils.EMPTY, null)));

        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeRequest("user", "password", "password")));

        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeRequest("user", "Th!sIsT3st", "password")));

        val request = new PasswordChangeRequest("user", "Th!sIsT3st", "Th!sIsT3st");
        assertTrue(passwordValidationService.isValid(creds, request));
        passwordHistoryService.store(request);
        assertFalse(passwordValidationService.isValid(creds, request));
    }
}
