package org.apereo.cas.authentication.sync;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.config.LdapPasswordSynchronizationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.beans.BeanContainer;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link BaseLdapPasswordSynchronizationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    LdapPasswordSynchronizationConfiguration.class
},
    properties = {
        "cas.authn.password-sync.ldap[0].enabled=true",
        "cas.authn.password-sync.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.authn.password-sync.ldap[0].base-dn=dc=example,dc=org",
        "cas.authn.password-sync.ldap[0].search-filter=cn={user}",
        "cas.authn.password-sync.ldap[0].bind-dn=cn=Directory Manager",
        "cas.authn.password-sync.ldap[0].bind-credential=password",
        "cas.authn.password-sync.ldap[0].password-synchronization-failure-fatal=true",
        "cas.authn.password-sync.ldap[0].password-attribute=userPassword"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseLdapPasswordSynchronizationTests {
    @Autowired
    @Qualifier("ldapPasswordSynchronizers")
    protected BeanContainer<AuthenticationPostProcessor> ldapPasswordSynchronizers;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @BeforeAll
    public static void setup() throws Exception {
        val localhost = new LDAPConnection("localhost", 10389, "cn=Directory Manager", "password");
        localhost.connect("localhost", 10389);
        localhost.bind("cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(localhost, "ou=people,dc=example,dc=org");
    }
}
