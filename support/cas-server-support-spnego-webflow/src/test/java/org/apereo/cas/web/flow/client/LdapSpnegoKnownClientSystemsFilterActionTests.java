package org.apereo.cas.web.flow.client;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(locations = {"classpath:/spnego.properties", "classpath:/spnego-ldap.properties"})
@Slf4j
public class LdapSpnegoKnownClientSystemsFilterActionTests extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @Before
    public void setup() {
        LdapIntegrationTestsOperations.checkContinuousIntegrationBuild(false);
    }

    @BeforeClass
    public static void bootstrap() throws Exception {
        LdapIntegrationTestsOperations.checkContinuousIntegrationBuild(false);
        LdapIntegrationTestsOperations.initDirectoryServer(1381);
    }
}
