package org.apereo.cas.mfa.accepto.web.flow.qr;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
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
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccepttoQRCodeAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    AccepttoMultifactorAuthenticationConfiguration.class,
    AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration.class,
    AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
},
    properties = {
        "cas.authn.mfa.acceptto.apiUrl=http://localhost:5001",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "spring.mail.testConnection=false",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
public class AccepttoQRCodeAuthenticationHandlerTests {
    @Test
    public void verifyOperation() throws Exception {
        val handler = new AccepttoQRCodeAuthenticationHandler(mock(ServicesManager.class), PrincipalFactoryUtils.newPrincipalFactory());
        assertTrue(handler.supports(AccepttoEmailCredential.class));
        val credential = new AccepttoEmailCredential("cas@example.org");
        assertTrue(handler.supports(credential));
        assertNotNull(handler.authenticate(credential));
    }
}
