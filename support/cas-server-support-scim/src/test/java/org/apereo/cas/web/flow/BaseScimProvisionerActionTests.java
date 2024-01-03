package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasScimAutoConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseScimProvisionerActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    CasWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasScimAutoConfiguration.class
})
public abstract class BaseScimProvisionerActionTests {
    @Autowired
    @Qualifier(PrincipalProvisioner.BEAN_NAME)
    protected PrincipalProvisioner principalProvisioner;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SCIM_PROVISIONING_PRINCIPAL)
    protected Action principalScimProvisionerAction;
}
