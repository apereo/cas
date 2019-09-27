package org.apereo.cas.pm.history;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    PasswordManagementConfiguration.class,
    CasCoreUtilConfiguration.class
},
    properties = {
        "cas.authn.pm.enabled=true",
        "cas.authn.pm.history.enabled=true",
        "cas.authn.pm.history.groovy.location=classpath:PasswordHistoryService.groovy"
    })
@Tag("Groovy")
public class GroovyPasswordHistoryServiceTests {
    @Autowired
    @Qualifier("passwordHistoryService")
    private PasswordHistoryService passwordHistoryService;

    @Test
    public void verifyValidity() {
        val request = new PasswordChangeRequest("casuser", "password", "password");
        assertFalse(passwordHistoryService.exists(request));
        assertTrue(passwordHistoryService.store(request));
        assertTrue(passwordHistoryService.fetchAll().isEmpty());
        assertTrue(passwordHistoryService.fetch("casuser").isEmpty());
    }
}
