package org.apereo.cas.nativex.features;

import lombok.NoArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

import java.security.Security;

/**
 * This is {@link BouncyCastleProviderFeature}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@NoArgsConstructor
public class BouncyCastleProviderFeature extends BaseCasNativeImageFeature {
    @Override
    public void afterRegistration(final AfterRegistrationAccess access) {
        log("Registering BouncyCastle security provider");
        RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
        Security.addProvider(new BouncyCastleProvider());
    }
}
