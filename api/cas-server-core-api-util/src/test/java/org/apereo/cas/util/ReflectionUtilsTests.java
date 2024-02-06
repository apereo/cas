package org.apereo.cas.util;

import org.apereo.cas.util.crypto.DecodableCipher;
import org.apereo.cas.util.crypto.EncodableCipher;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import java.time.Clock;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ReflectionUtils}.
 *
 * @author Lars Grefer
 * @since 6.6.0
 */
@Tag("Utility")
class ReflectionUtilsTests {

    @Test
    void findSubclassesInPackage() {
        val utilClasses = ReflectionUtils.findSubclassesInPackage(Object.class, ReflectionUtils.class.getPackageName());
        assertThat(utilClasses).contains(ReflectionUtils.class);
        assertThat(utilClasses).doesNotContain(Object.class);
    }

    @Test
    void findClassesWithAnnotationsInPackage() {
        val tagClasses = ReflectionUtils.findClassesWithAnnotationsInPackage(Set.of(FunctionalInterface.class), "org.apereo.cas");
        assertThat(tagClasses).contains(LogMessageSummarizer.class);
        assertThat(tagClasses).contains(DecodableCipher.class);
        assertThat(tagClasses).contains(EncodableCipher.class);
    }

    @Test
    void findClassBySimpleNameInPackage() {
        val applicationContext = ReflectionUtils.findClassBySimpleNameInPackage(ApplicationContext.class.getSimpleName(), "org.springframework");
        assertThat(applicationContext).isPresent();
        assertThat(applicationContext).contains(ApplicationContext.class);
    }

    @Test
    void findSystemClasses() {
        val utilClasses = ReflectionUtils.findSubclassesInPackage(Clock.class, Clock.class.getPackageName());
        assertThat(utilClasses).doesNotContain(Clock.class);
        assertThat(utilClasses).hasSizeGreaterThanOrEqualTo(5);
    }
}
