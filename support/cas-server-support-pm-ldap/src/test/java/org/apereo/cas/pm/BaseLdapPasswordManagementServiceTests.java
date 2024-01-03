package org.apereo.cas.pm;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.LdapPasswordManagementAutoConfiguration;
import org.apereo.cas.config.PasswordManagementAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link BaseLdapPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    LdapPasswordManagementAutoConfiguration.class,
    PasswordManagementAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class
})
public abstract class BaseLdapPasswordManagementServiceTests {
    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    protected PasswordManagementService passwordChangeService;
}
