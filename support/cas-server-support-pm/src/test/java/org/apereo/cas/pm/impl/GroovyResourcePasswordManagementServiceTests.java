package org.apereo.cas.pm.impl;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    PasswordManagementConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.groovy.location=classpath:/GroovyPasswordMgmt.groovy"
})
@Tag("Groovy")
public class GroovyResourcePasswordManagementServiceTests {

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Test
    public void verifyFindEmail() {
        assertNotNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build()));
    }

    @Test
    public void verifyFindUser() {
        assertNotNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().username("casuser@example.org").build()));
    }

    @Test
    public void verifyChangePassword() {
        assertTrue(passwordChangeService.change(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "password"),
            new PasswordChangeRequest("casuser", "password", "password")));
    }
}
