package org.apereo.cas.util;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import static org.apereo.cas.util.LdapTestProperties.*;

/**
 * This is {@link LdapTest}. Common properties used for LDAP testing.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "ldap.host=localhost",
    "ldap.port=10389",
    "ldap.bindDn=cn=Directory Manager",
    "ldap.bindPassword=password",
    "ldap.managerDn=cn=Directory Manager,dc=example,dc=org",
    "ldap.managerPassword=Password",
    "ldap.baseDn=dc=example,dc=org",
    "ldap.url=ldap:/${ldap.host}:${ldap.port}",
    "ldap.baseDn=dc=example,dc=org",
    "ldap.peopleDn=ou=people,${ldap.baseDn}"
    })
public interface LdapTest {
    @BeforeAll
    @SneakyThrows
    static void initializeTest() {
        val environment = getEnvironment();
        val path = environment.getProperty("ldap.test.resource", "");
        if (!path.isBlank()) {
            @Cleanup
            val localhost = new LDAPConnection(host(), port(), bindDn(), bindPass());
            localhost.connect(host(), port());
            localhost.bind(bindDn(), bindPass());
            LdapIntegrationTestsOperations.populateEntries(localhost,
                new ClassPathResource(path).getInputStream(),
                environment.getProperty("ldap.test.dnPrefix", "") + baseDn());
        }
    }
}
