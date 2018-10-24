package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@TestPropertySource(locations = {"classpath:/spnego.properties", "classpath:/spnego-ldap-ci.properties"})
@Category(LdapCategory.class)
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {

    @BeforeAll
    public static void bootstrap() throws Exception {
        val c = new LDAPConnection("localhost", 10389,
            "cn=Directory Manager", "password");
        LdapIntegrationTestsOperations.populateDefaultEntries(c, "ou=people,dc=example,dc=org");
    }
}
