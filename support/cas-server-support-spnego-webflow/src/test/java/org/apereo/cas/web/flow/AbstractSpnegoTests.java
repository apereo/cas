package org.apereo.cas.web.flow;

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
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.SpnegoAutoConfiguration;
import org.apereo.cas.config.SpnegoWebflowAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AbstractSpnegoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    SpnegoAutoConfiguration.class,
    SpnegoWebflowAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryStubConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class
}, properties = {
    "cas.authn.spnego.system.kerberos-conf=classpath:kerb5.conf",
    "cas.authn.spnego.system.login-conf=classpath:jaas.conf"
})
public abstract class AbstractSpnegoTests {

    @Autowired
    @Qualifier("negociateSpnego")
    protected Action negociateSpnegoAction;

    @Autowired
    @Qualifier("spnego")
    protected Action spnegoAction;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
