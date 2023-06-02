package org.apereo.cas.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ReflectionUtils}.
 *
 * @author Lars Grefer
 * @since 6.6.0
 */
@Tag("Utility")
public class ReflectionUtilsTests {

    @Test
    void findSubclassesInPackage() {
        Collection<Class<?>> utilClasses = ReflectionUtils.findSubclassesInPackage(Object.class, ReflectionUtils.class.getPackageName());

        assertThat(utilClasses).contains(ReflectionUtils.class);
        assertThat(utilClasses).doesNotContain(Object.class);
    }

    @Test
    void findClassesWithAnnotationsInPackage() {
        Collection<Class<?>> tagClasses = ReflectionUtils.findClassesWithAnnotationsInPackage(Set.of(Tag.class), "org.apereo.cas");

        assertThat(tagClasses).contains(ReflectionUtilsTests.class);
        assertThat(tagClasses).doesNotContain(ReflectionUtils.class);
    }

    @Test
    void findClassBySimpleNameInPackage() {
        Optional<Class<?>> applicationContext = ReflectionUtils.findClassBySimpleNameInPackage("ApplicationContext", "org.springframework");

        assertThat(applicationContext).isPresent();
        assertThat(applicationContext).contains(ApplicationContext.class);
    }
}
