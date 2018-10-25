package org.apereo.cas.pm.impl;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    PasswordManagementConfiguration.class})
@TestPropertySource(locations = {"classpath:/pm.properties"})
public class JsonResourcePasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Test
    public void verifyUserEmailCanBeFound() {
        val email = passwordChangeService.findEmail("casuser");
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyUserEmailCanNotBeFound() {
        val email = passwordChangeService.findEmail("casusernotfound");
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        val questions = passwordChangeService.getSecurityQuestions("casuser");
        assertEquals(2, questions.size());

    }

    @Test
    public void verifyUserPasswordChange() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        val res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }

    @Test
    public void verifyPasswordValidationService() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Test@1234");
        bean.setPassword("Test@1234");
        val isValid = passwordValidationService.isValid(c, bean);
        assertTrue(isValid);
    }
}
