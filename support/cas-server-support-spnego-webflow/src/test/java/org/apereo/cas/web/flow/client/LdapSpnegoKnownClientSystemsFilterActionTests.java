package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningStandaloneCondition;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(locations = {"classpath:/spnego.properties", "classpath:/spnego-ldap.properties"})
@ConditionalIgnore(condition = RunningStandaloneCondition.class)
public class LdapSpnegoKnownClientSystemsFilterActionTests extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.initDirectoryServer(1381);
    }
}
