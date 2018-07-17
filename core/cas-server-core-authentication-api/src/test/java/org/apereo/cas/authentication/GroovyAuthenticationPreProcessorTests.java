package org.apereo.cas.authentication;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyAuthenticationPreProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class GroovyAuthenticationPreProcessorTests {
    @Test
    public void verifyAction() {
        val g = new GroovyAuthenticationPreProcessor(new ClassPathResource("GroovyPreProcessor.groovy"));
        val transaction = mock(AuthenticationTransaction.class);
        when(transaction.getPrimaryCredential()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
        assertTrue(g.process(transaction));
        assertTrue(g.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}
