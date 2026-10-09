package org.apereo.cas.nativex.features;

import module java.base;
import lombok.NoArgsConstructor;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

/**
 * This is {@link DefaultNativeImageFeature}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@NoArgsConstructor
public class DefaultNativeImageFeature extends BaseCasNativeImageFeature {
    @Override
    public void afterRegistration(final AfterRegistrationAccess access) {
        val groovySystem = access.findClassByName("groovy.lang.GroovySystem");
        if (groovySystem != null) {
            log("Initializing Groovy metaclass registry before parallel native image analysis");
            try {
                Class.forName(groovySystem.getName(), true, groovySystem.getClassLoader());
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException("Unable to initialize Groovy before native image analysis", e);
            }
        }
        try {
            log("Registering BouncyCastle security provider");
            RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
        } catch (final Throwable e) {
            log("Unable to register BouncyCastle security provider: " + e.getMessage());
        } finally {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
