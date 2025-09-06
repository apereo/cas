package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.clouddirectory.AmazonCloudDirectoryRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonCloudDirectoryAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = {
    "cas.authn.cloud-directory.username-attribute-name=username",
    "cas.authn.cloud-directory.password-attribute-name=password"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
class AmazonCloudDirectoryAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyAction() throws Throwable {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username",
            List.of("casuser"), "password", List.of("Mellon")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon"), mock(Service.class)));
    }

    @Test
    void verifyNoPassAttr() {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username", List.of("casuser")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertThrows(AccountNotFoundException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456"), mock(Service.class)));
    }

    @Test
    void verifyNoMatch() {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username",
            List.of("casuser"), "password", List.of("Mellon")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertThrows(FailedLoginException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456"), mock(Service.class)));
    }
}
