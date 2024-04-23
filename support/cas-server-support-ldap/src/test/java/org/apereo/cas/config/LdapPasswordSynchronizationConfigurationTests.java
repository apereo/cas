package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapPasswordSynchronizationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasLdapAuthenticationAutoConfiguration.class
},
    properties = {
        "cas.authn.password-sync.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.authn.password-sync.ldap[0].base-dn=dc=example,dc=org",
        "cas.authn.password-sync.ldap[0].search-filter=cn={user}",
        "cas.authn.password-sync.ldap[0].bind-dn=cn=Directory Manager",
        "cas.authn.password-sync.ldap[0].bind-credential=password",
        "cas.authn.password-sync.ldap[0].enabled=true"
    })
@Tag("Ldap")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 10389)
class LdapPasswordSynchronizationConfigurationTests {
    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @Test
    void verifyOperation() throws Throwable {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertFalse(authenticationEventExecutionPlan.getAuthenticationPostProcessors(transaction).isEmpty());

    }
}
