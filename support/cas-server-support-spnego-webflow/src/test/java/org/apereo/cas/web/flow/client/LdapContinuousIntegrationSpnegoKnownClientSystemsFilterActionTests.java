package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.util.LdapTest;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import static org.apereo.cas.constants.test.Ldap.*;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Ldap")
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests implements LdapTest {

    @BeforeAll
    public static void bootstrap() throws Exception {
        val c = new LDAPConnection(getHost(), getPort(), getBindDn(), getBindPass());
        LdapIntegrationTestsOperations.populateDefaultEntries(c, getBaseDn());
    }
}
