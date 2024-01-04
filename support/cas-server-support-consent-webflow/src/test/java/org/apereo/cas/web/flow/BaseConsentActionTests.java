package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.config.CasConsentWebflowAutoConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
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
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseConsentActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    CasConsentCoreAutoConfiguration.class,
    CasConsentWebflowAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseConsentActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CONFIRM_CONSENT)
    protected Action confirmConsentAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CHECK_CONSENT_REQUIRED)
    protected Action checkConsentRequiredAction;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;
    @Autowired
    protected CasConfigurationProperties casProperties;
}
