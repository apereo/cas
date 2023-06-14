package org.apereo.cas.nativex;

import org.apereo.cas.util.spring.boot.BannerContributor;
import lombok.val;
import org.graalvm.home.Version;
import org.springframework.core.env.Environment;
import java.util.Formatter;

/**
 * This is {@link CasNativeBannerContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasNativeBannerContributor implements BannerContributor {
    @Override
    public void contribute(final Formatter formatter, final Environment environment) {
        val javaVendor = System.getProperty("java.vendor.version");
        if (javaVendor.contains("GraalVM")) {
            val version = Version.getCurrent().toString();
            formatter.format("Graal VM SDK Version: %s%n", version);
        }
    }
}
