package org.apereo.cas.authentication.bypass;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFATrigger")
public class ChainingMultifactorAuthenticationProviderBypassEvaluatorTests {

    @Test
    public void verifyOperation() {
        val input = mock(ChainingMultifactorAuthenticationProviderBypassEvaluator.class);
        doCallRealMethod().when(input)
            .addMultifactorAuthenticationProviderBypassEvaluator(any(MultifactorAuthenticationProviderBypassEvaluator[].class));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                input.addMultifactorAuthenticationProviderBypassEvaluator(mock(MultifactorAuthenticationProviderBypassEvaluator.class));
            }
        });
    }

}
