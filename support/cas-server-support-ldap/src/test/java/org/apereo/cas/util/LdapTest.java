package org.apereo.cas.util;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import static org.apereo.cas.constants.test.Ldap.*;

/**
 * This is {@link LdapTest}. Properties used for LDAP testing
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "ldap.host=localhost",
    "ldap.port=10839",
    "ldap.bindDn=cn=Directory Manager",
    "lsap.bindPassword=password",
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
    static void bootstrap() {
        @Cleanup
        val localhost = new LDAPConnection(HOST, PORT, BIND_DN, BIND_PASS);
        localhost.connect(HOST, PORT);
        localhost.bind(BIND_DN, BIND_PASS);
        LdapIntegrationTestsOperations.populateEntries(
            localhost,
            new ClassPathResource(System.getProperty("ldap.resource")).getInputStream(),
            BASE_DN);
        LdapIntegrationTestsOperations.populateEntries(localhost, new ClassPathResource(System.getProperty("ldap.test.resource")).getInputStream(),
            System.getProperty("ldap.test.dnPrefix", "") + BASE_DN);
    }

}
