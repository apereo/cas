package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class MultifactorAuthenticationProviderBeanTests {
    @Test
    public void verifyOperation() {
        val input = new MultifactorAuthenticationProviderBean(
            mock(MultifactorAuthenticationProviderFactoryBean.class),
            mock(DefaultListableBeanFactory.class), List.of());
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                input.onRefreshScopeRefreshed(new RefreshScopeRefreshedEvent());
            }
        });
    }
}
