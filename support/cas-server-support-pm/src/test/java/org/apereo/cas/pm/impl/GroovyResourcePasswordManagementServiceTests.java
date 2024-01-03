package org.apereo.cas.pm.impl;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.groovy.location=classpath:/GroovyPasswordMgmt.groovy"
})
@Tag("Groovy")
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
