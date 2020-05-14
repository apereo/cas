package org.apereo.cas.mfa.accepto;

import org.apereo.cas.config.AccepttoMultifactorAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseAccepttoMultifactorAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class BaseAccepttoMultifactorAuthenticationTests {
    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreUtilConfiguration.class,
        AccepttoMultifactorAuthenticationConfiguration.class,
        AccepttoMultifactorAuthenticationComponentSerializationConfiguration.class,
        AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration.class,
        AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
