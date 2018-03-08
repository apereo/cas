package org.apereo.cas.web.security;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.junit.BeforeClass;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasLdapGroupsUserDetailsManagerConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(locations = "classpath:/security-ldapuserdetails.properties")
public class CasLdapGroupsUserDetailsManagerConfigurerTests extends BaseCasLdapUserDetailsManagerConfigurerTests {
    @BeforeClass
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(1388);
    }
}
