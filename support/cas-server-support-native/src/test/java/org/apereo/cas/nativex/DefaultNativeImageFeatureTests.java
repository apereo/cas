package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.nativex.features.DefaultNativeImageFeature;
import lombok.val;
import org.graalvm.nativeimage.hosted.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultNativeImageFeatureTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class DefaultNativeImageFeatureTests {
    @Test
    void verifyOperation() {
        val results = new DefaultNativeImageFeature();
        assertDoesNotThrow(() -> results.afterRegistration(mock(Feature.AfterRegistrationAccess.class)));
    }
}
