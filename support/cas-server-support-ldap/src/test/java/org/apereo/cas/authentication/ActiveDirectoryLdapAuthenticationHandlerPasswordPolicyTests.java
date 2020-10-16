package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 * This test demonstrates password policy for AD by using user which soon to expire password.
 * @author Hal Deadman
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AUTHENTICATED",
    "cas.authn.ldap[0].bind-dn=Administrator@cas.example.org",
    "cas.authn.ldap[0].bind-credential=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_ADMIN_PASSWORD,
    "cas.authn.ldap[0].ldap-url=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_LDAP_URL,
    "cas.authn.ldap[0].useStartTls=true",
    "cas.authn.ldap[0].subtree-search=true",
    "cas.authn.ldap[0].base-dn=dc=cas,dc=example,dc=org",
    "cas.authn.ldap[0].follow-referrals=false",
    "cas.authn.ldap[0].principal-attribute-list=sAMAccountName,cn",
    "cas.authn.ldap[0].enhance-with-entry-resolver=true",
    "cas.authn.ldap[0].search-filter=(sAMAccountName={user})",
    "cas.authn.ldap[0].min-pool-size=0",
    "cas.authn.ldap[0].trust-store=" + BaseActiveDirectoryLdapAuthenticationHandlerTests.AD_TRUST_STORE,
    "cas.authn.ldap[0].trust-store-type=JKS",
    "cas.authn.ldap[0].trust-store-password=changeit",
    "cas.authn.ldap[0].hostname-verifier=DEFAULT",
    "cas.authn.ldap[0].trust-manager=DEFAULT",
    "cas.authn.ldap[0].password-policy.type=AD",
    "cas.authn.ldap[0].password-policy.enabled=true"
})
@EnabledIfPortOpen(port = 10390)
@Tag("Ldap")
public class ActiveDirectoryLdapAuthenticationHandlerPasswordPolicyTests extends BaseActiveDirectoryLdapAuthenticationHandlerTests {

    @Override
    protected String getUsername() {
        return "expirestomorrow";
    }

    @Test
    public void verifyAuthenticateWarnings() {
        assertNotEquals(handler.size(), 0);

        this.handler.forEach(Unchecked.consumer(h -> {
            val credential = new UsernamePasswordCredential(getUsername(), getSuccessPassword());
            val result = h.authenticate(credential);
            assertTrue(result.getWarnings() != null && !result.getWarnings().isEmpty());
            assertTrue(result.getWarnings().stream().anyMatch(messageDescriptor -> messageDescriptor.getCode().equals("password.expiration.warning")));
            assertNotNull(result.getPrincipal());
            assertEquals(credential.getUsername(), result.getPrincipal().getId());
            val attributes = result.getPrincipal().getAttributes();
            Arrays.stream(getPrincipalAttributes()).forEach(s -> assertTrue(attributes.containsKey(s)));
        }));
    }

}


