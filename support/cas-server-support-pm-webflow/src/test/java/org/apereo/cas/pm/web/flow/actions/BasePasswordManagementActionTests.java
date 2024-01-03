package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
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
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.config.PasswordManagementForgotUsernameConfiguration;
import org.apereo.cas.config.PasswordManagementWebflowConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BasePasswordManagementActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BasePasswordManagementActionTests.SharedTestConfiguration.class,
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.pm.core.enabled=true",
        "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
        "cas.authn.pm.forgot-username.mail.from=cas@example.org",
        "cas.authn.pm.reset.mail.from=cas@example.org",
        "cas.authn.pm.reset.security-questions-enabled=true"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BasePasswordManagementActionTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    protected CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    protected PasswordManagementService passwordManagementService;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS)
    protected Action verifySecurityQuestionsAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT)
    protected Action initPasswordResetAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INIT_PASSWORD_CHANGE)
    protected Action initPasswordChangeAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST)
    protected Action verifyPasswordResetRequestAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_VALIDATE_TOKEN)
    protected Action validatePasswordResetTokenAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS)
    protected Action sendPasswordResetInstructionsAction;

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory ticketFactory;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @ImportAutoConfiguration({
        AopAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        MailSenderValidatorAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        PasswordManagementConfiguration.class,
        PasswordManagementWebflowConfiguration.class,
        PasswordManagementForgotUsernameConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasThemesConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class
    })
    static class SharedTestConfiguration {
    }
}
