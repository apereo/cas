package org.apereo.cas.nativex.features;

import lombok.NoArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import java.security.Security;

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
