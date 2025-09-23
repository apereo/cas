package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityAdminApiEndpoint;
import org.apereo.cas.config.CasPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPasswordlessAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.configurer.BaseMultifactorWebflowConfigurerTests;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityMultifactorWebflowConfigurerTests {
    static {
        System.setProperty("DUO_SECURITY_ADMIN_SKEY", UUID.randomUUID().toString());
        System.setProperty("DUO_SECURITY_ADMIN_IKEY", UUID.randomUUID().toString());
    }

    @SpringBootTest(classes = {
        CasPasswordlessAuthenticationWebflowAutoConfiguration.class,
        CasPasswordlessAuthenticationAutoConfiguration.class,
        BaseDuoSecurityTests.SharedTestConfiguration.class
    },
        properties = {
            "management.endpoint.duoAdmin.access=UNRESTRICTED",
            "management.endpoints.web.exposure.include=*",

            "cas.authn.mfa.duo[0].duo-admin-secret-key=${#systemProperties['DUO_SECURITY_ADMIN_SKEY']}",
            "cas.authn.mfa.duo[0].duo-admin-integration-key=${#systemProperties['DUO_SECURITY_ADMIN_IKEY']}",
            "cas.authn.mfa.duo[0].duo-secret-key=cGKL1OndjtknbmVOWaFmisaghiNFEKXHxgXCJEBr",
            "cas.authn.mfa.duo[0].duo-integration-key=DIZXVRQD3OMZ6XXMNFQ9",
            "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com",
            "cas.authn.mfa.duo[0].trusted-device-enabled=true",
            "cas.authn.mfa.duo[0].passwordless-authentication-enabled=true",
            "cas.authn.mfa.trusted.core.device-registration-enabled=true"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    abstract static class BaseTests extends BaseMultifactorWebflowConfigurerTests {
        @Autowired
        @Qualifier("duoAdminApiEndpoint")
        private DuoSecurityAdminApiEndpoint duoAdminApiEndpoint;

        @Test
        void verifyAdminEndpoint() {
            assertNotNull(duoAdminApiEndpoint);
        }

        @Override
        protected FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry() {
            return applicationContext.getBean(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, FlowDefinitionRegistry.class);
        }

        @Override
        protected String getMultifactorEventId() {
            return DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER;
        }
    }

    @Nested
    class DefaultTests extends BaseTests {
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.duo[0].session-storage-type=TICKET_REGISTRY")
    class TicketRegistryStorageTests extends BaseTests {

    }
}

