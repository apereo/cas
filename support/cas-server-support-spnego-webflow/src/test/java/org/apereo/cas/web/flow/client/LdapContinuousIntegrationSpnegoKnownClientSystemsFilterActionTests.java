package org.apereo.cas.web.flow.client;

import com.unboundid.ldap.sdk.LDAPConnection;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.TestPropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(locations = {"classpath:/spnego.properties", "classpath:/spnego-ldap-ci.properties"})
@Slf4j
@Category(LdapCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeClass
    public static void bootstrap() throws Exception {
        final var c = new LDAPConnection("localhost", 10389,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(c, "ou=people,dc=example,dc=org");
    }
}
