package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
public class GroovyAuthenticationHandlerTests {
    @Test
    public void verifyOperation() {
        val resource = new ClassPathResource("GroovyAuthnHandler.groovy");
        val handler = new GroovyAuthenticationHandler("Test", mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), resource, 0);
        assertTrue(handler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(handler.supports(UsernamePasswordCredential.class));
        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertNotNull(result);
    }
}
