package org.apereo.cas.util.spring;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.MessageInterpolator;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SpringAwareMessageMessageInterpolatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
public class SpringAwareMessageMessageInterpolatorTests {

    @Test
    public void verifyOperation() {
        LocaleContextHolder.setLocale(Locale.getDefault());
        val polator = new SpringAwareMessageMessageInterpolator();
        val source = mock(MessageSource.class);
        when(source.getMessage(anyString(), any(Object[].class), any(Locale.class)))
            .thenThrow(new NoSuchMessageException("code"));
        polator.setMessageSource(source);
        val context = mock(MessageInterpolator.Context.class);
        val descriptor = mock(ConstraintDescriptor.class);
        when(descriptor.getAttributes()).thenReturn(Map.of());
        when(context.getConstraintDescriptor()).thenReturn(descriptor);
        val result = polator.interpolate("code", context);
        assertEquals("code", result);
    }

}
