package org.apereo.cas.authentication.sync;

import module java.base;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasLdapAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseLdapPasswordSynchronizationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreServicesAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasLdapAuthenticationAutoConfiguration.class
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
@ExtendWith(CasTestExtension.class)
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
