package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.azure.ad.config.AzureActiveDirectoryAuthenticationConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AzureActiveDirectoryAuthenticationConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
},
    properties = {
        "cas.authn.azure-active-directory.clientId=12345678-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.loginUrl=https://login.microsoftonline.com/common/"
    })
@Tag("Simple")
public class AzureActiveDirectoryAuthenticationHandlerTests {
    @Autowired
    @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
    private AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

    @Test
    public void verifyOperation() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
            "castest@hotmail.onmicrosoft.com", "1234567890");
        assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(c));
    }
}
