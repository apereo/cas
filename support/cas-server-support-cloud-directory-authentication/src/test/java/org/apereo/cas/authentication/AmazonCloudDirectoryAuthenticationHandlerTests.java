package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.clouddirectory.AmazonCloudDirectoryRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
    "cas.authn.cloud-directory.username-attribute-name=username",
    "cas.authn.cloud-directory.password-attribute-name=password"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("AmazonWebServices")
public class AmazonCloudDirectoryAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyAction() throws Exception {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username",
            List.of("casuser"), "password", List.of("Mellon")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertNotNull(h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon")));
    }

    @Test
    public void verifyNoPassAttr() {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username", List.of("casuser")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertThrows(AccountNotFoundException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456")));
    }

    @Test
    public void verifyNoMatch() {
        val repository = mock(AmazonCloudDirectoryRepository.class);
        when(repository.getUser(anyString())).thenReturn(CollectionUtils.wrap("username",
            List.of("casuser"), "password", List.of("Mellon")));
        val h = new AmazonCloudDirectoryAuthenticationHandler(StringUtils.EMPTY, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), repository, casProperties.getAuthn().getCloudDirectory());
        assertThrows(FailedLoginException.class,
            () -> h.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456")));
    }
}
