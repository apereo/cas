package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.config.PasswordManagementWebflowConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BasePasswordManagementActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class,
    PasswordManagementConfiguration.class,
    PasswordManagementWebflowConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCookieConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasWebflowContextConfiguration.class
})
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    "spring.mail.testConnection=true",
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
    "cas.authn.pm.reset.mail.from=cas@example.org",
    "cas.authn.pm.reset.securityQuestionsEnabled=true"
    })
public abstract class BasePasswordManagementActionTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("passwordChangeService")
    protected PasswordManagementService passwordManagementService;

    @Autowired
    @Qualifier("verifySecurityQuestionsAction")
    protected Action verifySecurityQuestionsAction;

    @Autowired
    @Qualifier("initPasswordResetAction")
    protected Action initPasswordResetAction;

    @Autowired
    @Qualifier("verifyPasswordResetRequestAction")
    protected Action verifyPasswordResetRequestAction;

    @Autowired
    @Qualifier("sendPasswordResetInstructionsAction")
    protected Action sendPasswordResetInstructionsAction;
}
