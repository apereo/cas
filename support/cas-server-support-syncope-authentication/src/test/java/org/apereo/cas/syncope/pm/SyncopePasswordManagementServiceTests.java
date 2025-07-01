package org.apereo.cas.syncope.pm;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
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
 * This is {@link SyncopePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 18080)
@SpringBootTest(classes = BaseSyncopeTests.SharedTestConfiguration.class, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true",

    "cas.authn.pm.syncope.basic-auth-username=admin",
    "cas.authn.pm.syncope.basic-auth-password=password",
    "cas.authn.pm.syncope.url=http://localhost:18080/syncope",
    "cas.authn.pm.syncope.attribute-mappings.syncopeUserAttr_givenName=name",
    "cas.authn.pm.syncope.attribute-mappings.syncopeUserAttr_email=email",
    "cas.authn.pm.syncope.attribute-mappings.syncopeUserAttr_phoneNumber=phoneNumber",
    "cas.authn.pm.syncope.attribute-mappings.username=username"
})
class SyncopePasswordManagementServiceTests {
    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    private PasswordManagementService passwordChangeService;
    
    @Test
    void verifyFindEmail() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("casuser@syncope.org", email);
    }

    @Test
    void verifyFindPhone() throws Throwable {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("3477464523", ph);
    }

    @Test
    void verifyFindSecurityQuestions() {
        assertThrows(UnsupportedOperationException.class,
            () -> passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()));
    }

    @Test
    void verifyUpdateSecurityQuestions() {
        assertThrows(UnsupportedOperationException.class,
            () -> passwordChangeService.updateSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build()));
    }

    @Test
    void verifyUnlockSecurityQuestions() {
        assertThrows(UnsupportedOperationException.class,
            () -> passwordChangeService.unlockAccount(new BasicIdentifiableCredential("casuser")));
    }
}
