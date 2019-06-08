package org.apereo.cas.pm;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

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
    CasCoreUtilConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.policyPattern=^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}",
})
public class DefaultPasswordValidationServiceTests {
    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Test
    public void verifyValidity() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password");
        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeBean(StringUtils.EMPTY, null)));

        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeBean("password", "password")));

        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeBean("Th!sIsT3st", "password")));

        assertFalse(passwordValidationService.isValid(
            creds,
            new PasswordChangeBean("Th!sIsT3st", "Th!sIsT3st")));
    }
}
