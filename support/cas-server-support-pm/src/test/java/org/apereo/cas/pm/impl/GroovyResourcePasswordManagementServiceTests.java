package org.apereo.cas.pm.impl;

import module java.base;
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
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasPasswordManagementAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.groovy.location=classpath:/GroovyPasswordMgmt.groovy"
})
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
class GroovyResourcePasswordManagementServiceTests {

    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    private PasswordManagementService passwordChangeService;

    @Test
    void verifyFindEmail() throws Throwable {
        assertNotNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build()));
    }

    @Test
    void verifyFindUser() throws Throwable {
        assertNotNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser@example.org").build()));
    }

    @Test
    void verifyChangePassword() throws Throwable {
        val request = new PasswordChangeRequest("casuser", "current-psw".toCharArray(), "password".toCharArray(), "password".toCharArray());
        assertTrue(passwordChangeService.change(request));
    }

    @Test
    void verifySecurityQuestions() throws Throwable {
        val query = PasswordManagementQuery.builder().username("casuser@example.org").build();
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
        query.securityQuestion("Q1", "A1");
        assertDoesNotThrow(() -> passwordChangeService.updateSecurityQuestions(query));
    }
}
