package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.azure.ad.BaseAzureActiveDirectoryTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AzureActiveDirectoryAuthenticationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Azure")
@EnabledIfEnvironmentVariable(named = "AZURE_AD_USER_PASSWORD", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AZURE_AD_DOMAIN", matches = ".+")
class AzureActiveDirectoryAuthenticationTests {

    private static final String AZURE_AD_USER_PASSWORD;
    private static final String AZURE_AD_DOMAIN;

    static {
        AZURE_AD_USER_PASSWORD = System.getenv("AZURE_AD_USER_PASSWORD");
        AZURE_AD_DOMAIN = System.getenv("AZURE_AD_DOMAIN");
    }

    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad",
        "cas.authn.azure-active-directory.resource=https://unknown.example.org"
    })
    @Nested
    class UnknownResourceTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

        @Test
        void verifyOperationFails() {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@" + AZURE_AD_DOMAIN, "bf65hfg78");
            assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class)));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    class PublicClientTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;


        @Test
        void verifyOperation() throws Throwable {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@" + AZURE_AD_DOMAIN, AZURE_AD_USER_PASSWORD);
            val result = microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class));
            assertNotNull(result);
        }

        @Test
        void verifyOperationFails() {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@" + AZURE_AD_DOMAIN, "bf65hfg78");
            assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class)));
        }
    }
    
    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.client-secret=${#environmentVariables['AZURE_AD_CLIENT_SECRET']}",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    class ConfidentialClientTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

        @Test
        void verifyOperation() throws Throwable {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@" + AZURE_AD_DOMAIN, AZURE_AD_USER_PASSWORD);
            val result = microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class));
            assertNotNull(result);
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.azure-active-directory[0].client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.attribute-repository.azure-active-directory[0].client-secret=${#environmentVariables['AZURE_AD_CLIENT_SECRET']}",
        "cas.authn.attribute-repository.azure-active-directory[0].tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    class AttributeResolutionTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAttributeRepositories")
        protected List<PersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories;

        @Test
        void verifyOperation() {
            val repository = microsoftAzureActiveDirectoryAttributeRepositories.getFirst();
            val person = repository.getPerson("castest@" + AZURE_AD_DOMAIN);
            assertNotNull(person);
        }
    }

}
