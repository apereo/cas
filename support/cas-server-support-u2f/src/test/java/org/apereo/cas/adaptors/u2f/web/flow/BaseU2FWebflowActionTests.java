package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
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
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSchedulingConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FWebflowConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.U2FAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.execution.Action;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseU2FWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public abstract class BaseU2FWebflowActionTests {
    @Autowired
    @Qualifier("u2fSaveAccountRegistrationAction")
    protected Action u2fSaveAccountRegistrationAction;

    @Autowired
    @Qualifier("u2fCheckAccountRegistrationAction")
    protected Action u2fCheckAccountRegistrationAction;

    @Autowired
    @Qualifier("u2fStartRegistrationAction")
    protected Action u2fStartRegistrationAction;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    protected U2FDeviceRepository deviceRepository;

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreAuthenticationConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketComponentSerializationConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreTicketsSchedulingConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreConfiguration.class,
        U2FConfiguration.class,
        U2FAuthenticationComponentSerializationConfiguration.class,
        U2FAuthenticationEventExecutionPlanConfiguration.class,
        U2FAuthenticationMultifactorProviderBypassConfiguration.class,
        U2FWebflowConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @TestConfiguration("U2FTestConfiguration")
    @Lazy(false)
    public static class U2FTestConfiguration {
        @Bean
        public U2F u2fService() throws Exception {
            val cert = CertUtils.readCertificate(new ClassPathResource("cert.crt"));
            val r1 = new DeviceRegistration("keyhandle11", "publickey1", cert, 1);
            val u2f = mock(U2F.class);
            when(u2f.startRegistration(any(), any())).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                    return new U2F().startRegistration(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
                }
            });
            when(u2f.finishRegistration(any(), any())).thenReturn(r1);
            return u2f;
        }
    }
}
