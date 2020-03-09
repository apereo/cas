package org.apereo.cas.shell.commands.util;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.shell.Input;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateLdapConnectionCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@EnableScheduling
@DirtiesContext
@Tag("Ldap")
@EnabledIfPortOpen(port = 10389)
public class ValidateLdapConnectionCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyOperation() {
        val cmd = "validate-ldap_--url_ldap://localhost:10389_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
            + "--bindCredential_password_--searchFilter_cn=admin_--userPassword_password_--userAttributes_cn";

        val result = shell.evaluate(new Input() {
            @Override
            public String rawText() {
                return StringUtils.replace(cmd, "_", " ");
            }

            @Override
            public List<String> words() {
                return Arrays.asList(cmd.split("_"));
            }
        });
        assertTrue((Boolean) result);
    }
}
