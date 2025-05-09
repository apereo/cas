package org.apereo.cas.syncope.passwordless;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.syncope.BaseSyncopeTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopePasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 18080)
@SpringBootTest(
    classes = {
        BaseSyncopeTests.SharedTestConfiguration.class,
        CasPasswordlessAuthenticationAutoConfiguration.class
    },
    properties = {
        "cas.authn.passwordless.accounts.syncope.basic-auth-username=admin",
        "cas.authn.passwordless.accounts.syncope.basic-auth-password=password",
        "cas.authn.passwordless.accounts.syncope.url=http://localhost:18080/syncope",
        "cas.authn.passwordless.accounts.syncope.attribute-mappings.syncopeUserAttr_givenName=name",
        "cas.authn.passwordless.accounts.syncope.attribute-mappings.syncopeUserAttr_email=email",
        "cas.authn.passwordless.accounts.syncope.attribute-mappings.syncopeUserAttr_phoneNumber=phoneNumber",
        "cas.authn.passwordless.accounts.syncope.attribute-mappings.username=username"
    })
class SyncopePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    void verifyOperation() throws Throwable {
        val user = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
            .builder()
            .username("syncopecas")
            .build());
        assertTrue(user.isPresent());
        val passwordlessUser = user.get();
        assertEquals("syncopecas", passwordlessUser.getUsername());
        assertEquals("ApereoCAS", passwordlessUser.getName());
        assertEquals("syncopecas@syncope.org", passwordlessUser.getEmail());
        assertEquals("1234567890", passwordlessUser.getPhone());
    }
}
