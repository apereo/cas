package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    LdapPasswordSynchronizationConfiguration.class
},
    properties = {
        "cas.authn.password-sync.ldap[0].ldap-url=ldap://localhost:10389",
        "cas.authn.password-sync.ldap[0].baseDn=dc=example,dc=org",
        "cas.authn.password-sync.ldap[0].search-filter=cn={user}",
        "cas.authn.password-sync.ldap[0].bind-dn=cn=Directory Manager",
        "cas.authn.password-sync.ldap[0].bind-credential=password",
        "cas.authn.password-sync.ldap[0].enabled=true"
    })
@Tag("Ldap")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 10389)
public class LdapPasswordSynchronizationConfigurationTests {
    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @Test
    public void verifyOperation() {
        val transaction = DefaultAuthenticationTransaction.of(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertFalse(authenticationEventExecutionPlan.getAuthenticationPostProcessors(transaction).isEmpty());

    }
}
