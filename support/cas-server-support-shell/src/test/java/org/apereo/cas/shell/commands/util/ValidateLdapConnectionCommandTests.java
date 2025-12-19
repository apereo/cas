package org.apereo.cas.shell.commands.util;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateLdapConnectionCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Ldap")
@EnabledIfListeningOnPort(port = 10389)
class ValidateLdapConnectionCommandTests extends BaseCasShellCommandTests {
    @Test
    void verifyOperation() {
        val cmd = "validate-ldap_--url_ldap://localhost:10389_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
                  + "--bindCredential_password_--searchFilter_cn=admin_--userPassword_password_--userAttributes_cn";
        assertDoesNotThrow(() -> runShellCommand(getUnderscoreToSpaceInput(cmd)));
    }

    @Test
    void verifyNoFilterOperation() {
        val cmd = "validate-ldap_--url_ldap://localhost:10389_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
                  + "--bindCredential_password_--userPassword_password_--userAttributes_cn";
        assertDoesNotThrow(() -> runShellCommand(getUnderscoreToSpaceInput(cmd)));
    }
    
    @Test
    void verifyFailsOperation() {
        val cmd = "validate-ldap_--url_ldap://localhost:10389_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
                  + "--bindCredential_password_--searchFilter_badfilter_--userPassword_password_--userAttributes_cn";

        val input = getUnderscoreToSpaceInput(cmd);
        assertDoesNotThrow(() -> runShellCommand(input));
    }

    @Test
    void verifyBadUrlOperation() {
        val cmd = "validate-ldap_--url_ldap://localhost:10399_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
                  + "--bindCredential_password_--searchFilter_badfilter_--userPassword_password_--userAttributes_cn";

        val input = getUnderscoreToSpaceInput(cmd);
        assertDoesNotThrow(() -> runShellCommand(input));
    }

    @Test
    void verifyNoResult() {
        val cmd = "validate-ldap_--url_ldap://localhost:10389_--baseDn_dc=example,dc=org_--bindDn_cn=Directory Manager_"
                  + "--bindCredential_password_--searchFilter_cn=123456_--userPassword_password_--userAttributes_cn";

        val input = getUnderscoreToSpaceInput(cmd);
        assertDoesNotThrow(() -> runShellCommand(input));
    }


    private static Supplier<String> getUnderscoreToSpaceInput(final String cmd) {
        return () -> Strings.CI.replace(cmd, "_", " ");
    }

}
