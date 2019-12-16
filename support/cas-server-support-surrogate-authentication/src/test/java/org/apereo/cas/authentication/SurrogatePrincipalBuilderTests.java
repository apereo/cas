package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogatePrincipalBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(MockitoExtension.class)
public class SurrogatePrincipalBuilderTests {
    @Test
    public void verifyOperationWithNoService() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository());
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal());
        assertNotNull(p);
    }

    @Test
    public void verifyOperationWithService() {
        val surrogatePrincipalBuilder = new SurrogatePrincipalBuilder(
            PrincipalFactoryUtils.newPrincipalFactory(), CoreAuthenticationTestUtils.getAttributeRepository());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(new DenyAllAttributeReleasePolicy());
        val p = surrogatePrincipalBuilder.buildSurrogatePrincipal("surrogate", CoreAuthenticationTestUtils.getPrincipal(), registeredService);
        assertNotNull(p);
    }
}
