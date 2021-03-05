package org.apereo.cas.pm.impl;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
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
        "cas.authn.pm.core.policy-pattern=^Test1.+"
    })
@Tag("FileSystem")
public class JsonResourcePasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Test
    public void verifyUserEmailCanBeFound() {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyUserCanBeFound() {
        val user = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("casuser@example.org").build());
        assertEquals("casuser", user);
    }

    @Test
    public void verifyUserPhoneCanBeFound() {
        val phone = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("1234567890", phone);
    }

    @Test
    public void verifyUserEmailCanNotBeFound() {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casusernotfound").build());
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals(2, questions.size());
        assertTrue(passwordChangeService.getSecurityQuestions(
            PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build()).isEmpty());
    }

    @Test
    public void verifyUserPasswordChange() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        val res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }

    @Test
    public void verifyUserPasswordChangeFail() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword");
        var res = passwordChangeService.change(c, bean);
        assertFalse(res);
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("unknown");
        res = passwordChangeService.change(c, bean);
        assertFalse(res);

        bean.setPassword(bean.getConfirmedPassword());
        c.setUsername(UUID.randomUUID().toString());
        res = passwordChangeService.change(c, bean);
        assertFalse(res);
    }

    @Test
    public void verifyPasswordValidationService() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setUsername(c.getUsername());
        bean.setConfirmedPassword("Test1@1234");
        bean.setPassword("Test1@1234");
        val isValid = passwordValidationService.isValid(c, bean);
        assertTrue(isValid);
    }
}
