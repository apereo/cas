package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SchedulingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Utility")
public class SchedulingUtilsTests {
    @Test
    public void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        assertNotNull(SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext));
    }
}
