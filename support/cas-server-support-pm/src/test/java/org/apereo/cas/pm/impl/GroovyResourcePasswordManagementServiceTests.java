package org.apereo.cas.pm.impl;

import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
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
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
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
