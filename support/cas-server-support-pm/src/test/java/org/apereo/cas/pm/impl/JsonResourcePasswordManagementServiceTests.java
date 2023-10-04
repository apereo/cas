package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryStubConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    MailSenderAutoConfiguration.class,
    PasswordManagementConfiguration.class
},
    properties = {
        "cas.authn.pm.json.location=classpath:jsonResourcePassword.json",
        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.core.password-policy-pattern=^Test1.+"
    })
@Tag("FileSystem")
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
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword".toCharArray());
        var res = passwordChangeService.change(bean);
        assertFalse(res);
        bean.setConfirmedPassword("newPassword".toCharArray());
        bean.setPassword("unknown".toCharArray());
        res = passwordChangeService.change(bean);
        assertFalse(res);

        bean.setPassword(bean.getConfirmedPassword());
        c.setUsername(UUID.randomUUID().toString());
        res = passwordChangeService.change(bean);
        assertFalse(res);
    }
    @Test
    void verifyPasswordValidationService() throws Throwable {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setUsername(c.getUsername());
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
}
