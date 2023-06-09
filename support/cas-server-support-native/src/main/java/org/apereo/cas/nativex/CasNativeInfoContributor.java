package org.apereo.cas.nativex;

import lombok.val;
import org.graalvm.home.Version;
import org.graalvm.nativeimage.ImageInfo;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

import java.util.HashMap;

/**
 * This is {@link CasNativeInfoContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeInfoContributor implements InfoContributor {
    @Override
    public void contribute(final Info.Builder builder) {
        val nativeImageInfo = new HashMap<>();
        nativeImageInfo.put("graalVMVersion", Version.getCurrent().toString());
        nativeImageInfo.put("executable", ImageInfo.isExecutable());
        builder.withDetail("nativeImage", nativeImageInfo);
    }
}
