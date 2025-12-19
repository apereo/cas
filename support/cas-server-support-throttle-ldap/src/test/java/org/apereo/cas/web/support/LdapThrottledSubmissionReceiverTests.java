package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.adaptors.ldap.LdapIntegrationTestsOperations;
import org.apereo.cas.config.CasLdapThrottlingAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.unboundid.ldap.sdk.LDAPConnection;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LdapThrottledSubmissionReceiverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTest(classes = {
    CasLdapThrottlingAutoConfiguration.class,
    BaseThrottledSubmissionHandlerInterceptorAdapterTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.throttle.ldap.ldap-url=ldap://localhost:11389",
        "cas.authn.throttle.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.throttle.ldap.search-filter=cn={0}",
        "cas.authn.throttle.ldap.bind-dn=cn=admin,dc=example,dc=org",
        "cas.authn.throttle.ldap.bind-credential=P@ssw0rd",
        "cas.authn.throttle.ldap.account-locked-attribute=postalCode"
    })
@Tag("LdapAuthentication")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 11389)
class LdapThrottledSubmissionReceiverTests {
    private static final int LDAP_PORT = 11389;

    @Autowired
    @Qualifier("ldapThrottledSubmissionReceiver")
    private ThrottledSubmissionReceiver ldapThrottledSubmissionReceiver;
    
    @BeforeAll
    public static void bootstrap() throws Exception {
        ClientInfoHolder.setClientInfo(ClientInfo.from(new MockHttpServletRequest()));
        val localhost = new LDAPConnection("localhost", LDAP_PORT,
            "cn=admin,dc=example,dc=org", "P@ssw0rd");
        LdapIntegrationTestsOperations.populateEntries(localhost,
            new ClassPathResource("ldif/openldap-throttle.ldif").getInputStream(),
            "ou=people,dc=example,dc=org",
            new BindConnectionInitializer("cn=admin,dc=example,dc=org", new Credential("P@ssw0rd")));
    }

    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> {
            val submission = ThrottledSubmission.builder().username("throttled").build();
            ldapThrottledSubmissionReceiver.receive(submission);
        });
    }
}
