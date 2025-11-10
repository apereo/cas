package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasPasswordManagementAutoConfiguration.class
},
    properties = {
        "cas.authn.pm.json.location=classpath:jsonResourcePassword.json",
        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.core.password-policy-pattern=^Test1.+"
    })
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
class JsonResourcePasswordManagementServiceTests {
    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier(PasswordValidationService.BEAN_NAME)
    private PasswordValidationService passwordValidationService;

    @Test
    void verifyUserEmailCanBeFound() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("casuser@example.org", email);
    }

    @Test
    void verifyUserCanBeFound() throws Throwable {
        val user = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("casuser@example.org").build());
        assertEquals("casuser", user);
    }

    @Test
    void verifyUserPhoneCanBeFound() throws Throwable {
        val phone = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("1234567890", phone);
    }

    @Test
    void verifyUserEmailCanNotBeFound() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casusernotfound").build());
        assertNull(email);
    }

    @Test
    void verifyUnlock() throws Throwable {
        val credentials = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        assertTrue(passwordChangeService.unlockAccount(credentials));
    }

    @Test
    void verifyUserQuestionsCanBeFound() throws Throwable {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals(2, questions.size());
        assertTrue(passwordChangeService.getSecurityQuestions(
            PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build()).isEmpty());
    }
    @Test
    void verifyUserPasswordChange() throws Throwable {
        val bean = new PasswordChangeRequest();
        bean.setUsername("casuser");
        bean.setConfirmedPassword("newPassword".toCharArray());
        bean.setPassword("newPassword".toCharArray());
        val res = passwordChangeService.change(bean);
        assertTrue(res);
    }
    @Test
    void verifyUserPasswordChangeFail() throws Throwable {
        val credential = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword".toCharArray());
        var res = passwordChangeService.change(bean);
        assertFalse(res);
        bean.setConfirmedPassword("newPassword".toCharArray());
        bean.setPassword("unknown".toCharArray());
        res = passwordChangeService.change(bean);
        assertFalse(res);

        bean.setPassword(bean.getConfirmedPassword());
        credential.setUsername(UUID.randomUUID().toString());
        res = passwordChangeService.change(bean);
        assertFalse(res);
    }
    @Test
    void verifyPasswordValidationService() throws Throwable {
        val credential = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setUsername(credential.getUsername());
        bean.setConfirmedPassword("Test1@1234".toCharArray());
        bean.setPassword("Test1@1234".toCharArray());
        val isValid = passwordValidationService.isValid(bean);
        assertTrue(isValid);
    }
    @Test
    void verifySecurityQuestions() throws Throwable {
        val query = PasswordManagementQuery.builder().username("casuser").build();
        assertDoesNotThrow(() -> {
            query.securityQuestion("Q1", "A1");
            query.securityQuestion("Q2", "A2");
            passwordChangeService.updateSecurityQuestions(query);
        });
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
    }

    @Test
    void verifyToken() throws Throwable {
        val token = passwordChangeService.createToken(PasswordManagementQuery.builder().username("casuser").build());
        assertNotNull(token);
        val parsed = passwordChangeService.parseToken(token);
        assertEquals("casuser", parsed);
    }
}
