package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.azure.ad.BaseAzureActiveDirectoryTests;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
public class AzureActiveDirectoryAuthenticationTests {

    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad",
        "cas.authn.azure-active-directory.resource=https://unknown.example.org"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class UnknownResourceTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

        @Test
        public void verifyOperationFails() {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@onmicrosoft.com", "bf65hfg78 ");
            assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class)));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class PublicClientTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

        @Test
        public void verifyOperation() throws Exception {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@misaghmoayyedhotmail.onmicrosoft.com", "zVh86iUtwQLP");
            val result = microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class));
            assertNotNull(result);
        }

        @Test
        public void verifyOperationFails() {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@onmicrosoft.com", "bf65hfg78 ");
            assertThrows(FailedLoginException.class, () -> microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class)));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.azure-active-directory.client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.azure-active-directory.client-secret=Ro58Q~NbOEInGNGAEdxHGWJ3QkS0jVTLP1fuLcg-",
        "cas.authn.azure-active-directory.login-url=https://login.microsoftonline.com/common/",
        "cas.authn.azure-active-directory.tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class ConfidentialClientTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAuthenticationHandler")
        protected AuthenticationHandler microsoftAzureActiveDirectoryAuthenticationHandler;

        @Test
        public void verifyOperation() throws Exception {
            val credentials = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(
                "castest@misaghmoayyedhotmail.onmicrosoft.com", "zVh86iUtwQLP");
            val result = microsoftAzureActiveDirectoryAuthenticationHandler.authenticate(credentials, mock(Service.class));
            assertNotNull(result);
        }
    }
    
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.azure-active-directory[0].client-id=d430f66f-bc3b-4e2d-a9bf-bf6c7ded8b7e",
        "cas.authn.attribute-repository.azure-active-directory[0].client-secret=Ro58Q~NbOEInGNGAEdxHGWJ3QkS0jVTLP1fuLcg-",
        "cas.authn.attribute-repository.azure-active-directory[0].tenant=2bbf190a-1ee3-487d-b39f-4d5038acf9ad"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class AttributeResolutionTests extends BaseAzureActiveDirectoryTests {
        @Autowired
        @Qualifier("microsoftAzureActiveDirectoryAttributeRepositories")
        protected List<IPersonAttributeDao> microsoftAzureActiveDirectoryAttributeRepositories;

        @Test
        public void verifyOperation() throws Exception {
            val repository = microsoftAzureActiveDirectoryAttributeRepositories.get(0);
            val person = repository.getPerson("castest@misaghmoayyedhotmail.onmicrosoft.com");
            assertNotNull(person);
        }
    }

}
