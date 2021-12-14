package org.apereo.cas.pm.impl;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.pm.PasswordManagementServiceProvider;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.services.RegexRegisteredService;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        PasswordManagementConfiguration.class,
        MailSenderAutoConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreUtilConfiguration.class
}, properties = {
        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.history.core.enabled=true",
        "cas.authn.pm.core.policy-pattern=^Th!.+{8,10}"
})
@Tag("PasswordOps")
class DefaultPasswordManagementServiceProviderTests {

    @Autowired
    private PasswordManagementServiceProvider passwordManagementServiceProvider;

    @Test
    void verifyReturnService() {
        assertNotNull(passwordManagementServiceProvider.getPasswordChangeService(new RegexRegisteredService()));
    }
}
