package org.apereo.cas.syncope.pm;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.syncope.BaseSyncopeTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import javax.security.auth.login.FailedLoginException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SyncopePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 18080)
@SpringBootTest(classes = BaseSyncopeTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.syncope.url=http://localhost:18080/syncope",

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

    @Autowired
    @Qualifier("syncopeAuthenticationHandlers")
    private BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers;

    @Test
    void verifyFindEmail() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("mustChangePasswordUser").build());
        assertEquals("mustChangePasswordUser@syncope.org", email);
    }

    @Test
    void verifyFindPhone() throws Throwable {
        val ph = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("mustChangePasswordUser").build());
        assertEquals("2345678901", ph);
    }

    @Test
    void verifyFindSecurityQuestions() throws Throwable {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("syncopecas").build());
        assertEquals(1, questions.size());
        assertEquals("What is your favorite city?", questions.keySet().stream().toList().getFirst());
    }

    @Test
    void verifyUpdateSecurityQuestions() {
        assertThrows(UnsupportedOperationException.class, () -> passwordChangeService.updateSecurityQuestions(
            PasswordManagementQuery.builder().username("mustChangePasswordUser").build()));
    }

    @Test
    void verifyUnlockAccount() throws Throwable {
        assertTrue(passwordChangeService.unlockAccount(new BasicIdentifiableCredential("syncopesuspend4")));
    }

    @Test
    void verifyMustChangePasswordPasses() throws Throwable {
        assertNotNull(syncopeAuthenticationHandlers);
        val syncopeAuthenticationHandler = syncopeAuthenticationHandlers.first();
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
            "mustChangePasswordUser",
            "ChangePassword");
        assertThrows(AccountPasswordMustChangeException.class,
            () -> syncopeAuthenticationHandler.authenticate(credential, mock(Service.class)));

        val passwordChangeRequest = new PasswordChangeRequest(
            "mustChangePasswordUser",
            "ChangePassword".toCharArray(),
            "Password123!".toCharArray(),
            "Password123!".toCharArray());

        assertTrue(passwordChangeService.change(passwordChangeRequest));
        assertThrows(FailedLoginException.class,
            () -> syncopeAuthenticationHandler.authenticate(credential, mock(Service.class)));
        credential.setPassword("Password123!".toCharArray());
        assertDoesNotThrow(() -> syncopeAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }
}
