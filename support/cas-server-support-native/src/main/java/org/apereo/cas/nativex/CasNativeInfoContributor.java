package org.apereo.cas.nativex;

import module java.base;
import lombok.val;
import org.graalvm.home.Version;
import org.graalvm.nativeimage.ImageInfo;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

/**
 * This is {@link CasNativeInfoContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeInfoContributor implements InfoContributor {
    private static final String GRAALVM_VERSION = Version.getCurrent().toString();

    @Override
    public void contribute(final Info.Builder builder) {
        val nativeImageInfo = new HashMap<>();
        nativeImageInfo.put("graalVMVersion", GRAALVM_VERSION);
        nativeImageInfo.put("executable", ImageInfo.isExecutable());
        builder.withDetail("nativeImage", nativeImageInfo);
    }
}
