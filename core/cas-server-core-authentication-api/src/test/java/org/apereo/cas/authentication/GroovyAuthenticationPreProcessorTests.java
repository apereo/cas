package org.apereo.cas.authentication;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyAuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GroovyAuthentication")
class GroovyAuthenticationPreProcessorTests {
    @Test
    void verifyAction() throws Throwable {
        val processor = new GroovyAuthenticationPreProcessor(new ClassPathResource("GroovyPreProcessor.groovy"));
        val transaction = mock(AuthenticationTransaction.class);
        val creds = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        when(transaction.getPrimaryCredential()).thenReturn(Optional.of(creds));
        assertTrue(processor.process(transaction));
        assertTrue(processor.supports(creds));
        processor.destroy();
    }
}
